package com.techmomentum.wc2026.ui.team

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.domain.usecase.GetAlbumUseCase
import com.techmomentum.wc2026.domain.usecase.StickerSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamAlbumUiState(
    val team: Team? = null,
    val slots: List<StickerSlot> = emptyList(),
    val loading: Boolean = true,
)

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
            _uiState.update { it.copy(team = team, slots = slots, loading = false) }
        }
    }
}
