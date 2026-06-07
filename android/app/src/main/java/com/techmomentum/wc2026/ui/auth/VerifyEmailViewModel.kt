package com.techmomentum.wc2026.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.domain.usecase.DetermineSignedInGateUseCase
import com.techmomentum.wc2026.domain.usecase.SignedInGate
import com.techmomentum.wc2026.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyEmailUiState(
    val email: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val destinationRoute: String? = null,
)

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val determineSignedInGate: DetermineSignedInGateUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        VerifyEmailUiState(email = authRepository.currentUser?.email.orEmpty()),
    )
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    fun resendVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null) }
            authRepository.sendEmailVerification().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(loading = false, message = "Verification email sent. Check your inbox.")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, message = e.message) }
                },
            )
        }
    }

    fun checkVerified() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null) }
            authRepository.reloadAuthUser().fold(
                onSuccess = {
                    if (!authRepository.isEmailVerified) {
                        _uiState.update {
                            it.copy(
                                loading = false,
                                message = "Email not verified yet. Open the link in your inbox, then try again.",
                            )
                        }
                        return@launch
                    }
                    val profile = userRepository.observeUserProfile().first()
                    val route = when (determineSignedInGate(profile)) {
                        SignedInGate.NeedsProfileCompletion -> Routes.COMPLETE_PROFILE
                        SignedInGate.Ready -> Routes.HOME
                        SignedInGate.NeedsEmailVerification -> Routes.VERIFY_EMAIL
                    }
                    _uiState.update {
                        it.copy(loading = false, destinationRoute = route, message = "Email verified!")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, message = e.message) }
                },
            )
        }
    }
}
