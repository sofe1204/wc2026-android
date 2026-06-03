package com.techmomentum.wc2026.ui.team

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.domain.usecase.GetAlbumUseCase
import com.techmomentum.wc2026.domain.usecase.StickerSlot
import com.techmomentum.wc2026.utils.GameConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamAlbumUiState(
    val team: Team? = null,
    val crestSlot: StickerSlot? = null,
    val playerSlots: List<StickerSlot> = emptyList(),
    val ownedCount: Int = 0,
    val percent: Float = 0f,
    val loading: Boolean = true,
) {
    val total: Int
        get() {
            val count = (if (crestSlot != null) 1 else 0) + playerSlots.size
            return count.takeIf { it > 0 } ?: GameConstants.STICKERS_PER_TEAM
        }

    val allSlots: List<StickerSlot>
        get() = buildList {
            crestSlot?.let { add(it) }
            addAll(playerSlots)
        }
}

@HiltViewModel
class TeamAlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlbumUseCase: GetAlbumUseCase,
) : ViewModel() {
    private val teamId: String = savedStateHandle.get<String>("teamId") ?: ""

    private val _uiState = MutableStateFlow(TeamAlbumUiState())
    val uiState: StateFlow<TeamAlbumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (team, slots) = getAlbumUseCase.getTeamAlbum(teamId)
            val ownedCount = slots.allSlots.count { it.owned != null }
            val total = slots.allSlots.size.takeIf { it > 0 } ?: GameConstants.STICKERS_PER_TEAM
            val percent = ownedCount.toFloat() / total * 100f
            _uiState.update {
                it.copy(
                    team = team,
                    crestSlot = slots.crestSlot,
                    playerSlots = slots.playerSlots,
                    ownedCount = ownedCount,
                    percent = percent,
                    loading = false,
                )
            }
        }
    }
}
