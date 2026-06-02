package com.techmomentum.wc2026.ui.bootstrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.firebase.FirebaseSessionCoordinator
import com.techmomentum.wc2026.data.repository.AppAuthState
import com.techmomentum.wc2026.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BootstrapState {
    data object Loading : BootstrapState()
    data object Ready : BootstrapState()
    data class Error(val message: String) : BootstrapState()
}

@HiltViewModel
class SessionBootstrapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionCoordinator: FirebaseSessionCoordinator,
) : ViewModel() {
    private val _state = MutableStateFlow<BootstrapState>(BootstrapState.Loading)
    val state: StateFlow<BootstrapState> = _state.asStateFlow()

    init {
        runBootstrap()
    }

    fun retry() = runBootstrap()

    private fun runBootstrap() {
        viewModelScope.launch {
            _state.value = BootstrapState.Loading
            when (val auth = authRepository.authState().first()) {
                is AppAuthState.SignedIn -> {
                    val result = sessionCoordinator.bootstrapSignedInUser()
                    result.fold(
                        onSuccess = { profile ->
                            if (profile.success) {
                                _state.value = BootstrapState.Ready
                            } else {
                                _state.value = BootstrapState.Error(
                                    profile.message.ifBlank { "Could not sync profile with Firebase." },
                                )
                            }
                        },
                        onFailure = { e ->
                            _state.value = BootstrapState.Error(
                                e.message ?: "Could not sync profile with Firebase.",
                            )
                        },
                    )
                }
                AppAuthState.Guest, AppAuthState.Unauthenticated -> {
                    _state.value = BootstrapState.Ready
                }
            }
        }
    }
}
