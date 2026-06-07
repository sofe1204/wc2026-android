package com.techmomentum.wc2026.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.LeaderboardResult
import com.techmomentum.wc2026.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LeaderboardTab { GLOBAL, COUNTRY }

data class LeaderboardUiState(
    val tab: LeaderboardTab = LeaderboardTab.GLOBAL,
    val loading: Boolean = true,
    val error: String? = null,
    val result: LeaderboardResult = LeaderboardResult(),
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: LeaderboardTab) = _uiState.update { it.copy(tab = tab) }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { profileRepository.getLeaderboard() }.fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(loading = false, result = result) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }
}
