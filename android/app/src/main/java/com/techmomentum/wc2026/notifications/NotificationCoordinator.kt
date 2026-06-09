package com.techmomentum.wc2026.notifications

import com.google.firebase.auth.FirebaseAuth
import com.techmomentum.wc2026.data.local.AppPreferences
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.repository.AppAuthState
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.data.repository.FcmTokenRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NotificationCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val scheduler: NotificationScheduler,
    private val fcmTokenRepository: FcmTokenRepository,
    private val preferences: AppPreferences,
    @Named("application") private val appScope: CoroutineScope,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        appScope.launch {
            authRepository.authState()
                .flatMapLatest { state ->
                    when (state) {
                        is AppAuthState.SignedIn -> userRepository.observeUserProfile()
                        else -> flowOf(null)
                    }
                }
                .distinctUntilChanged()
                .collect { profile ->
                    if (auth.currentUser == null) {
                        onSignedOut()
                        return@collect
                    }
                    onProfileUpdated(profile)
                }
        }
    }

    suspend fun onNotificationsEnabled() {
        if (!preferences.notificationsEnabled) return
        onProfileUpdated(loadProfile())
    }

    suspend fun onNotificationsDisabled() {
        scheduler.cancelAll()
        fcmTokenRepository.clearToken()
    }

    private suspend fun onProfileUpdated(profile: UserProfile?) {
        scheduler.reschedule(profile)
        if (preferences.notificationsEnabled && profile?.profileComplete == true) {
            fcmTokenRepository.registerCurrentToken()
        }
    }

    private fun onSignedOut() {
        scheduler.cancelAll()
        appScope.launch { fcmTokenRepository.clearToken() }
    }

    private suspend fun loadProfile(): UserProfile? =
        runCatching { userRepository.observeUserProfile().first() }.getOrNull()
}
