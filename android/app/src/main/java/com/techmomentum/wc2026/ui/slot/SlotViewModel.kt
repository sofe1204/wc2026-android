package com.techmomentum.wc2026.ui.slot

import android.app.Activity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.local.AppPreferences
import com.techmomentum.wc2026.data.model.SlotCell
import com.techmomentum.wc2026.data.model.SlotGridPosition
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.data.slot.SlotCellBuilder
import com.techmomentum.wc2026.data.slot.SlotGridParser
import com.techmomentum.wc2026.data.slot.SlotSymbolsWarmup
import com.techmomentum.wc2026.data.slot.SlotWinChecker
import com.techmomentum.wc2026.debug.DebugAgentLog
import com.techmomentum.wc2026.utils.GameConstants
import com.techmomentum.wc2026.utils.RewardEligibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SlotPhase {
    Idle,
    Spinning,
    Settled,
}

data class SlotUiState(
    val phase: SlotPhase = SlotPhase.Idle,
    val spinGeneration: Int = 0,
    val finalCells: List<List<SlotCell>> = emptyList(),
    val symbolPool: List<SlotSymbol> = emptyList(),
    val symbolsReady: Boolean = false,
    val spinsRemaining: Int = 0,
    val packsWonToday: Int = 0,
    val winningCells: Set<SlotGridPosition> = emptySet(),
    val serverIsWin: Boolean = false,
    val localIsWin: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val message: String? = null,
    val slotSpinAdAvailable: Boolean = true,
    val slotSpinAdCooldownMinutes: Int = 0,
    val imageRefreshGeneration: Int = 0,
    val spinResultReady: Boolean = false,
) {
    val isAnimating: Boolean get() = phase == SlotPhase.Spinning
    val isWin: Boolean get() = phase == SlotPhase.Settled && serverIsWin && localIsWin
    val atSlotPackCap: Boolean get() = packsWonToday >= GameConstants.DAILY_SLOT_PACK_REWARD_CAP
    val canSpin: Boolean get() = RewardEligibility.canSpinSlots(spinsRemaining, packsWonToday)
}

@HiltViewModel
class SlotViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val catalogRepository: CatalogRepository,
    private val userRepository: UserRepository,
    private val rewardedAdManager: RewardedAdManager,
    private val slotSymbolsWarmup: SlotSymbolsWarmup,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SlotUiState())
    val uiState: StateFlow<SlotUiState> = _uiState.asStateFlow()

    private var symbolList: List<SlotSymbol> = emptyList()

    init {
        viewModelScope.launch {
            loadSymbols()
            val preview = SlotCellBuilder.randomPreview(symbolList)
            _uiState.update {
                it.copy(
                    symbolsReady = true,
                    symbolPool = symbolList,
                    finalCells = preview,
                    phase = SlotPhase.Idle,
                )
            }
            slotSymbolsWarmup.warmIfNeeded()
            _uiState.update { it.copy(imageRefreshGeneration = it.imageRefreshGeneration + 1) }
            rewardedAdManager.load()
        }
        viewModelScope.launch {
            userRepository.observeUserProfile().collect { profile ->
                if (profile != null) {
                    val lastAd = profile.lastRewardedSlotSpinAtEpochMs
                    val packsWonToday = profile.slotRewardPacksWonToday
                    _uiState.update {
                        it.copy(
                            spinsRemaining = profile.slotSpinsRemaining,
                            packsWonToday = packsWonToday,
                            slotSpinAdAvailable = RewardEligibility.isSlotSpinAdRewardAvailable(
                                lastAd,
                                packsWonToday,
                            ),
                            slotSpinAdCooldownMinutes = RewardEligibility.slotSpinAdCooldownMinutesRemaining(lastAd),
                        )
                    }
                }
            }
        }
    }

    fun spin() {
        val current = _uiState.value
        if (current.phase == SlotPhase.Spinning || !current.canSpin) return

        viewModelScope.launch {
            ensureSymbols()
            _uiState.update {
                it.copy(
                    phase = SlotPhase.Spinning,
                    spinGeneration = it.spinGeneration + 1,
                    message = null,
                    winningCells = emptySet(),
                    serverIsWin = false,
                    localIsWin = false,
                    spinResultReady = false,
                    symbolPool = symbolList,
                )
            }
            val result = runCatching { rewardsRepository.spinSlotMachine() }
            result.fold(
                onSuccess = { slotResult -> applySpinResult(slotResult) },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            phase = SlotPhase.Settled,
                            spinResultReady = true,
                            message = e.message ?: "Spin failed.",
                        )
                    }
                },
            )
        }
    }

    fun markSpinSettled() {
        viewModelScope.launch {
            while (!_uiState.value.spinResultReady) {
                delay(50)
            }
            _uiState.update {
                it.copy(
                    phase = SlotPhase.Settled,
                    hapticsEnabled = appPreferences.hapticsEnabled,
                )
            }
        }
    }

    fun watchAdForSpins(activity: Activity, rewardedAdManager: RewardedAdManager) {
        if (!_uiState.value.slotSpinAdAvailable) return
        rewardedAdManager.show(
            activity = activity,
            onReward = {
                viewModelScope.launch {
                    runCatching { rewardsRepository.claimRewardedSlotSpins() }
                        .onSuccess { result ->
                            _uiState.update { s ->
                                s.copy(message = result.message.ifBlank { "Done!" })
                            }
                        }
                        .onFailure { e ->
                            _uiState.update { s -> s.copy(message = e.message) }
                        }
                }
            },
            onDismiss = {},
        )
    }

    fun slotBitmap(url: String): ImageBitmap? = slotSymbolsWarmup.bitmapFor(url)

    private suspend fun applySpinResult(result: SlotResult) {
        ensureSymbols()
        val spinIds = result.grid
        if (!SlotGridParser.isValid(spinIds)) {
            _uiState.update {
                it.copy(
                    phase = SlotPhase.Settled,
                    spinResultReady = true,
                    message = "Could not read spin result. Please try again.",
                )
            }
            return
        }

        val finalCells = SlotCellBuilder.build(spinIds, symbolList)
        val winningCells = SlotWinChecker.winningCells(spinIds)
        val localIsWin = winningCells.isNotEmpty()
        val serverIsWin = result.isWin

        if (localIsWin != serverIsWin) {
            DebugAgentLog.log(
                location = "SlotViewModel.applySpinResult",
                message = "spinId mismatch",
                hypothesisId = "slot-sync",
                data = mapOf(
                    "localIsWin" to localIsWin,
                    "serverIsWin" to serverIsWin,
                    "spinIds" to spinIds.flatten().joinToString(","),
                ),
            )
        }

        val message = resolveSpinMessage(result, localIsWin, serverIsWin)
        slotSymbolsWarmup.warmIfNeeded()

        _uiState.update {
            it.copy(
                finalCells = finalCells,
                winningCells = if (localIsWin && serverIsWin) winningCells else emptySet(),
                serverIsWin = serverIsWin,
                localIsWin = localIsWin,
                spinsRemaining = result.spinsRemaining,
                packsWonToday = result.packsWonToday,
                message = message,
                spinResultReady = true,
                imageRefreshGeneration = it.imageRefreshGeneration + 1,
            )
        }
    }

    private fun resolveSpinMessage(result: SlotResult, localIsWin: Boolean, serverIsWin: Boolean): String {
        if (localIsWin != serverIsWin) {
            return "Spin sync error — try again"
        }
        if (result.message.isNotBlank()) return result.message
        return when {
            result.rewardGranted -> "You won a sticker pack!"
            localIsWin && serverIsWin -> "Line match! Daily pack limit reached."
            else -> "No match — try again!"
        }
    }

    private suspend fun loadSymbols() {
        symbolList = catalogRepository.getSlotSymbols().filter { it.isActive }
    }

    private suspend fun ensureSymbols() {
        if (symbolList.isEmpty()) {
            loadSymbols()
        }
        slotSymbolsWarmup.warmIfNeeded()
    }

}
