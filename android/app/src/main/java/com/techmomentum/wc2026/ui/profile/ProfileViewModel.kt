package com.techmomentum.wc2026.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val seeding: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rewardsRepository: RewardsRepository,
    userRepository: UserRepository,
) : ViewModel() {
    val email: String
        get() = if (authRepository.isGuest) "guest@demo.local" else authRepository.currentUser?.email.orEmpty()

    val displayName: String
        get() = if (authRepository.isGuest) {
            "Guest Collector"
        } else {
            authRepository.currentUser?.displayName.orEmpty()
        }

    val profile = userRepository.observeUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun signOut() = authRepository.signOut()

    fun seedFirestore() {
        viewModelScope.launch {
            _uiState.update { it.copy(seeding = true, message = null) }
            val results = listOf(
                runCatching { rewardsRepository.seedTeams() },
                runCatching { rewardsRepository.seedPlayers() },
                runCatching { rewardsRepository.seedStickers() },
            )
            val errors = results.mapNotNull { it.exceptionOrNull()?.message }
            _uiState.update {
                it.copy(
                    seeding = false,
                    message = if (errors.isEmpty()) "Seed complete (admin required)" else errors.joinToString(),
                )
            }
        }
    }
}
