package com.techmomentum.wc2026.ui.bootstrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.firebase.FirebaseSessionCoordinator
import com.techmomentum.wc2026.data.slot.SlotSymbolsWarmup
import com.techmomentum.wc2026.data.repository.AppAuthState
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.domain.usecase.DetermineSignedInGateUseCase
import com.techmomentum.wc2026.domain.usecase.SignedInGate
import com.techmomentum.wc2026.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BootstrapState {
    data object Loading : BootstrapState()
    data class Ready(val destinationRoute: String) : BootstrapState()
    data class Error(val message: String) : BootstrapState()
}

@HiltViewModel
class SessionBootstrapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionCoordinator: FirebaseSessionCoordinator,
    private val userRepository: UserRepository,
    private val determineSignedInGate: DetermineSignedInGateUseCase,
    private val slotSymbolsWarmup: SlotSymbolsWarmup,
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
                    val slotWarmup = async { slotSymbolsWarmup.warmIfNeeded() }
                    val result = sessionCoordinator.bootstrapSignedInUser()
                    result.fold(
                        onSuccess = { profile ->
                            if (profile.success) {
                                val userProfile = userRepository.observeUserProfile().first()
                                val route = when (determineSignedInGate(userProfile)) {
                                    SignedInGate.NeedsEmailVerification -> Routes.VERIFY_EMAIL
                                    SignedInGate.NeedsProfileCompletion -> Routes.COMPLETE_PROFILE
                                    SignedInGate.Ready -> Routes.HOME
                                }
                                slotWarmup.await()
                                _state.value = BootstrapState.Ready(route)
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
                AppAuthState.Guest -> {
                    slotSymbolsWarmup.warmIfNeeded()
                    _state.value = BootstrapState.Ready(Routes.HOME)
                }
                AppAuthState.Unauthenticated -> _state.value = BootstrapState.Ready(Routes.AUTH)
            }
        }
    }
}
