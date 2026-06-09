package com.techmomentum.wc2026.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
import com.techmomentum.wc2026.domain.usecase.HomeState
import com.techmomentum.wc2026.domain.usecase.ObserveHomeStateUseCase
import com.techmomentum.wc2026.ui.pack.RevealedSticker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val showStickerReveal: Boolean = false,
    val revealed: List<RevealedSticker> = emptyList(),
    val revealIndex: Int = -1,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHomeState: ObserveHomeStateUseCase,
    private val rewardsRepository: RewardsRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    val homeState: StateFlow<HomeState> = observeHomeState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun claimRewardedAdStickers() = launchReward { rewardsRepository.claimRewardedAdStickers() }

    fun revealNextSticker() {
        _uiState.update { state ->
            val next = state.revealIndex + 1
            if (next >= state.revealed.size) state else state.copy(revealIndex = next)
        }
    }

    fun dismissStickerReveal() {
        _uiState.update {
            it.copy(
                showStickerReveal = false,
                revealed = emptyList(),
                revealIndex = -1,
            )
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun launchReward(block: suspend () -> CallableResult) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null) }
            runCatching { block() }.fold(
                onSuccess = { result -> handleRewardResult(result) },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, message = e.message) }
                },
            )
        }
    }

    private suspend fun handleRewardResult(result: CallableResult) {
        if (!result.success) {
            _uiState.update {
                it.copy(loading = false, message = result.message.ifBlank { "Could not claim reward." })
            }
            return
        }
        val revealed = loadRevealedStickers(result.stickerIds)
        if (revealed.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    loading = false,
                    message = null,
                    showStickerReveal = true,
                    revealed = revealed,
                    revealIndex = 0,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    loading = false,
                    message = result.message.ifBlank { "Stickers added to your album!" },
                )
            }
        }
    }

    private suspend fun loadRevealedStickers(stickerIds: List<String>): List<RevealedSticker> {
        if (stickerIds.isEmpty()) return emptyList()
        val stickers = catalogRepository.getStickers()
        val players = catalogRepository.getPlayers()
        return stickerIds.mapNotNull { id ->
            val sticker = stickers.firstOrNull { it.stickerId == id } ?: return@mapNotNull null
            RevealedSticker(sticker, players.firstOrNull { it.playerId == sticker.playerId })
        }
    }
}
