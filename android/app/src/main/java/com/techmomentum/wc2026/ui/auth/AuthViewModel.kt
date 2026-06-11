package com.techmomentum.wc2026.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.config.ProjectConfig
import com.techmomentum.wc2026.data.auth.GoogleAuthClient
import com.techmomentum.wc2026.data.firebase.FirebaseConfigState
import com.techmomentum.wc2026.data.firebase.FirebaseConnectionRepository
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
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

data class AuthUiState(
    val email: String = "",
    val confirmEmail: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignUp: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val destinationRoute: String? = null,
    val firebaseHint: String? = null,
    val googleSignInAvailable: Boolean = false,
    val googleSetupHint: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rewardsRepository: RewardsRepository,
    private val userRepository: UserRepository,
    private val determineSignedInGate: DetermineSignedInGateUseCase,
    private val googleAuthClient: GoogleAuthClient,
    connectionRepository: FirebaseConnectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AuthUiState(
            googleSignInAvailable = googleAuthClient.isConfigured,
            googleSetupHint = googleSetupMessage(),
        ),
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepository.configState.collect { config ->
                val hint = when (config) {
                    FirebaseConfigState.Placeholder ->
                        "Add google-services.json from Firebase Console (see google-services.json.example)."
                    FirebaseConfigState.ProjectMismatch ->
                        "google-services.json project does not match ${ProjectConfig.FIREBASE_PROJECT_ID}."
                    FirebaseConfigState.Ready -> null
                }
                _uiState.update { it.copy(firebaseHint = hint) }
            }
        }
    }

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onConfirmEmailChange(v: String) = _uiState.update { it.copy(confirmEmail = v) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v) }
    fun toggleMode() = _uiState.update {
        it.copy(isSignUp = !it.isSignUp, error = null, confirmEmail = "", confirmPassword = "")
    }

    fun getGoogleSignInIntent(): Intent? = googleAuthClient.getSignInIntent()

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val tokenResult = googleAuthClient.idTokenFromResult(data)
            tokenResult.fold(
                onSuccess = { token ->
                    authRepository.signInWithGoogle(token).fold(
                        onSuccess = { finishProfileSetup(createNewAccount = false) },
                        onFailure = { e -> showError(e.message) },
                    )
                },
                onFailure = { e -> showError(e.message) },
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isBlank() || !email.contains("@") || state.password.length < 6) {
            _uiState.update { it.copy(error = "Enter a valid email and password (6+ characters)") }
            return
        }
        if (state.isSignUp) {
            if (state.confirmEmail.trim() != email) {
                _uiState.update { it.copy(error = "Email addresses do not match.") }
                return
            }
            if (state.confirmPassword != state.password) {
                _uiState.update { it.copy(error = "Passwords do not match.") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val authResult = if (state.isSignUp) {
                authRepository.signUp(
                    email,
                    state.password,
                    email.substringBefore("@"),
                )
            } else {
                authRepository.signIn(email, state.password)
            }
            authResult.fold(
                onSuccess = { finishProfileSetup(createNewAccount = state.isSignUp) },
                onFailure = { e -> showError(e.message) },
            )
        }
    }

    private suspend fun finishProfileSetup(createNewAccount: Boolean = false) {
        val profile = rewardsRepository.ensureUserProfile(createNewAccount = createNewAccount)
        if (!profile.success) {
            showError(profile.message.ifBlank { "Could not set up your profile. Try again." })
            return
        }
        val route = resolvePostAuthRoute()
        _uiState.update {
            it.copy(loading = false, success = true, destinationRoute = route, error = null)
        }
    }

    private suspend fun resolvePostAuthRoute(): String {
        val userProfile = userRepository.observeUserProfile().first()
        return when (determineSignedInGate(userProfile)) {
            SignedInGate.NeedsProfileCompletion -> Routes.COMPLETE_PROFILE
            SignedInGate.Ready -> Routes.HOME
        }
    }

    private fun showError(message: String?) {
        _uiState.update {
            it.copy(loading = false, error = message ?: "Something went wrong. Try again.")
        }
    }

    private fun googleSetupMessage(): String? = googleAuthClient.configurationHint()
}
