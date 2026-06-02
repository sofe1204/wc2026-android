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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isSignUp: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val firebaseHint: String? = null,
    val googleSignInAvailable: Boolean = false,
    val googleSetupHint: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rewardsRepository: RewardsRepository,
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
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    fun onDisplayNameChange(v: String) = _uiState.update { it.copy(displayName = v) }
    fun toggleMode() = _uiState.update { it.copy(isSignUp = !it.isSignUp, error = null) }

    fun getGoogleSignInIntent(): Intent? = googleAuthClient.getSignInIntent()

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val tokenResult = googleAuthClient.idTokenFromResult(data)
            tokenResult.fold(
                onSuccess = { token ->
                    authRepository.signInWithGoogle(token).fold(
                        onSuccess = { finishProfileSetup() },
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
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val authResult = if (state.isSignUp) {
                authRepository.signUp(
                    email,
                    state.password,
                    state.displayName.trim().ifBlank { email.substringBefore("@") },
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

    fun continueAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            authRepository.signInAsGuest()
            rewardsRepository.ensureUserProfile()
            _uiState.update { it.copy(loading = false, success = true) }
        }
    }

    private suspend fun finishProfileSetup(createNewAccount: Boolean = false) {
        val profile = rewardsRepository.ensureUserProfile(createNewAccount = createNewAccount)
        if (profile.success) {
            _uiState.update { it.copy(loading = false, success = true, error = null) }
        } else {
            showError(profile.message.ifBlank { "Could not set up your profile. Try again." })
        }
    }

    private fun showError(message: String?) {
        _uiState.update {
            it.copy(loading = false, error = message ?: "Something went wrong. Try again.")
        }
    }

    private fun googleSetupMessage(): String? {
        if (googleAuthClient.isConfigured) return null
        return "Enable Google in Firebase Authentication (${ProjectConfig.GCP_PUBLIC_PROJECT_ID}), " +
            "add debug SHA-1, then re-download google-services.json."
    }
}
