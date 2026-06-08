package com.techmomentum.wc2026.data.firebase

import com.techmomentum.wc2026.data.repository.AppAuthState
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.RewardsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Keeps catalog cache and cloud profile in sync when auth mode changes.
 */
@Singleton
class FirebaseSessionCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val catalogRepository: CatalogRepository,
    private val rewardsRepository: RewardsRepository,
    private val connectionRepository: FirebaseConnectionRepository,
    @Named("application") private val appScope: CoroutineScope,
) {
    fun start() {
        appScope.launch {
            authRepository.authState()
                .map { state ->
                    when (state) {
                        is AppAuthState.SignedIn -> SessionMode.Cloud(state.user.uid)
                        AppAuthState.Unauthenticated -> SessionMode.None
                    }
                }
                .distinctUntilChanged()
                .collect { mode -> onSessionModeChanged(mode) }
        }
    }

    suspend fun bootstrapSignedInUser(): Result<com.techmomentum.wc2026.data.model.CallableResult> =
        runCatching {
            val profile = rewardsRepository.ensureUserProfile()
            catalogRepository.clearCache()
            connectionRepository.refreshConfigState()
            profile
        }

    private suspend fun onSessionModeChanged(mode: SessionMode) {
        catalogRepository.clearCache()
        connectionRepository.refreshConfigState()
        when (mode) {
            is SessionMode.Cloud -> rewardsRepository.ensureUserProfile()
            SessionMode.None -> Unit
        }
    }

    private sealed interface SessionMode {
        data object None : SessionMode
        data class Cloud(val uid: String) : SessionMode
    }
}
