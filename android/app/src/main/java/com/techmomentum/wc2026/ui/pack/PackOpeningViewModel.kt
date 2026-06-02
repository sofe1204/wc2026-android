package com.techmomentum.wc2026.ui.pack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.PackOpenResult
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RevealedSticker(
    val sticker: Sticker,
    val player: Player?,
)

data class PackOpeningUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val revealed: List<RevealedSticker> = emptyList(),
    val revealIndex: Int = -1,
    val finished: Boolean = false,
)

@HiltViewModel
class PackOpeningViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackOpeningUiState())
    val uiState: StateFlow<PackOpeningUiState> = _uiState.asStateFlow()

    fun openPack() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, revealed = emptyList(), revealIndex = -1) }
            runCatching { rewardsRepository.openStickerPack() }.fold(
                onSuccess = { result -> loadRevealed(result) },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    fun revealNext() {
        _uiState.update { state ->
            val next = state.revealIndex + 1
            if (next >= state.revealed.size) {
                state.copy(finished = true)
            } else {
                state.copy(revealIndex = next)
            }
        }
    }

    private suspend fun loadRevealed(result: PackOpenResult) {
        val stickers = catalogRepository.getStickers()
        val players = catalogRepository.getPlayers()
        val revealed = result.stickerIds.mapNotNull { id ->
            val sticker = stickers.firstOrNull { it.stickerId == id } ?: return@mapNotNull null
            RevealedSticker(sticker, players.firstOrNull { it.playerId == sticker.playerId })
        }
        _uiState.update {
            it.copy(loading = false, revealed = revealed, revealIndex = if (revealed.isNotEmpty()) 0 else -1)
        }
    }
}
