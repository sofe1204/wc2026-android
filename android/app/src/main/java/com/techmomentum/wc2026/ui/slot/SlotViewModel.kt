package com.techmomentum.wc2026.ui.slot

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SlotUiState(
    val grid: List<List<Player?>> = List(3) { List(3) { null } },
    val spinsRemaining: Int = 0,
    val packsWonToday: Int = 0,
    val isWin: Boolean = false,
    val loading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SlotViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val catalogRepository: CatalogRepository,
    private val rewardedAdManager: RewardedAdManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SlotUiState())
    val uiState: StateFlow<SlotUiState> = _uiState.asStateFlow()

    private var playersById: Map<String, Player> = emptyMap()

    init {
        viewModelScope.launch {
            playersById = catalogRepository.getPlayers().associateBy { it.playerId }
            rewardedAdManager.load()
        }
    }

    fun spin() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null) }
            runCatching { rewardsRepository.spinSlotMachine() }.fold(
                onSuccess = { applyResult(it) },
                onFailure = { e -> _uiState.update { it.copy(loading = false, message = e.message) } },
            )
        }
    }

    fun watchAdForSpins(activity: Activity, rewardedAdManager: RewardedAdManager) {
        rewardedAdManager.show(
            activity = activity,
            onReward = {
                viewModelScope.launch {
                    runCatching { rewardsRepository.claimRewardedSlotSpins() }
                        .onSuccess { result -> _uiState.update { s -> s.copy(message = result.message) } }
                }
            },
            onDismiss = {},
        )
    }

    private fun applyResult(result: SlotResult) {
        val grid = result.grid.map { row ->
            row.map { pid -> playersById[pid] }
        }
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
                        result.isWin -> "Win! Daily pack limit reached."
                        else -> "No match — try again!"
                    }
                },
            )
        }
    }
}
