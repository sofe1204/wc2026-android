package com.techmomentum.wc2026.ui.slot

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.utils.RewardEligibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class SlotUiState(
    val grid: List<List<SlotSymbol?>> = List(3) { List(3) { null } },
    val spinsRemaining: Int = 0,
    val packsWonToday: Int = 0,
    val isWin: Boolean = false,
    val loading: Boolean = false,
    val message: String? = null,
    val slotSpinAdAvailable: Boolean = true,
    val slotSpinAdCooldownMinutes: Int = 0,
)

@HiltViewModel
class SlotViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val catalogRepository: CatalogRepository,
    private val userRepository: UserRepository,
    private val rewardedAdManager: RewardedAdManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SlotUiState())
    val uiState: StateFlow<SlotUiState> = _uiState.asStateFlow()

    private var symbolsById: Map<String, SlotSymbol> = emptyMap()
    private var symbolList: List<SlotSymbol> = emptyList()

    init {
        viewModelScope.launch {
            loadSymbols()
            showPreviewGrid()
            rewardedAdManager.load()
        }
        viewModelScope.launch {
            userRepository.observeUserProfile().collect { profile ->
                if (profile != null) {
                    val lastAd = profile.lastRewardedSlotSpinAtEpochMs
                    _uiState.update {
                        it.copy(
                            spinsRemaining = profile.slotSpinsRemaining,
                            packsWonToday = profile.slotRewardPacksWonToday,
                            slotSpinAdAvailable = RewardEligibility.isSlotSpinAdAvailable(lastAd),
                            slotSpinAdCooldownMinutes = RewardEligibility.slotSpinAdCooldownMinutesRemaining(lastAd),
                        )
                    }
                }
            }
        }
    }

    fun spin() {
        viewModelScope.launch {
            ensureSymbols()
            _uiState.update { it.copy(loading = true, message = null, isWin = false) }
            runCatching { rewardsRepository.spinSlotMachine() }.fold(
                onSuccess = { applyResult(it) },
                onFailure = { e -> _uiState.update { it.copy(loading = false, message = e.message) } },
            )
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

    private suspend fun loadSymbols() {
        symbolList = catalogRepository.getSlotSymbols().filter { it.isActive }
        symbolsById = symbolList.associateBy { it.symbolId }
    }

    private suspend fun ensureSymbols() {
        if (symbolsById.isEmpty()) loadSymbols()
    }

    private fun showPreviewGrid() {
        if (symbolList.isEmpty()) return
        val grid = List(3) {
            List(3) { symbolList[Random.nextInt(symbolList.size)] }
        }
        _uiState.update { it.copy(grid = grid) }
    }

    private fun resolveSymbol(id: String): SlotSymbol? =
        symbolsById[id] ?: symbolList.find { it.playerId == id }

    private suspend fun applyResult(result: SlotResult) {
        ensureSymbols()
        val grid = result.grid.map { row -> row.map { resolveSymbol(it) } }
        _uiState.update {
            it.copy(
                grid = grid,
                spinsRemaining = result.spinsRemaining,
                packsWonToday = result.packsWonToday,
                isWin = result.isWin,
                loading = false,
                message = result.message.ifBlank {
                    when {
                        result.rewardGranted -> "You won a sticker pack!"
                        result.isWin -> "Diagonal match! Daily pack limit reached."
                        else -> "No diagonal match — try again!"
                    }
                },
            )
        }
    }
}
