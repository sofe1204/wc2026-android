package com.techmomentum.wc2026.ui.sticker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.data.model.UserSticker
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.UserStickersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StickerDetailUiState(
    val sticker: Sticker? = null,
    val player: Player? = null,
    val team: Team? = null,
    val owned: UserSticker? = null,
    val loading: Boolean = true,
)

@HiltViewModel
class StickerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
    private val userStickersRepository: UserStickersRepository,
) : ViewModel() {
    private val stickerId: String = savedStateHandle.get<String>("stickerId") ?: ""

    private val _uiState = MutableStateFlow(StickerDetailUiState())
    val uiState: StateFlow<StickerDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sticker = catalogRepository.getSticker(stickerId)
            val player = sticker?.let { catalogRepository.getPlayer(it.playerId) }
            val team = sticker?.let { catalogRepository.getTeam(it.teamId) }
            val owned = userStickersRepository.observeUserStickers().first()[stickerId]
            _uiState.update {
                it.copy(sticker = sticker, player = player, team = team, owned = owned, loading = false)
            }
        }
    }
}
