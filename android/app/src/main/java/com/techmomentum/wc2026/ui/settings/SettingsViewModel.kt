package com.techmomentum.wc2026.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.BuildConfig
import com.techmomentum.wc2026.data.firebase.FirebaseConnectionRepository
import com.techmomentum.wc2026.data.local.AppPreferences
import com.techmomentum.wc2026.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val firebaseSummary: String = "",
    val firebaseProjectId: String = "",
    val useFirebaseEmulators: Boolean = false,
    val usesProductionFirebase: Boolean = true,
    val isGuest: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val connectionRepository: FirebaseConnectionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _prefs = MutableStateFlow(
        SettingsUiState(
            soundEnabled = preferences.soundEnabled,
            hapticsEnabled = preferences.hapticsEnabled,
            notificationsEnabled = preferences.notificationsEnabled,
            isGuest = authRepository.isGuest,
        ),
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        _prefs,
        connectionRepository.connectionStatus(),
    ) { prefs, firebase ->
        prefs.copy(
            firebaseSummary = firebase.summary,
            firebaseProjectId = firebase.projectId,
            useFirebaseEmulators = firebase.useEmulators,
            usesProductionFirebase = !BuildConfig.USE_FIREBASE_EMULATORS,
            isGuest = authRepository.isGuest,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _prefs.value)

    fun setSound(enabled: Boolean) {
        preferences.soundEnabled = enabled
        _prefs.update { it.copy(soundEnabled = enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        preferences.hapticsEnabled = enabled
        _prefs.update { it.copy(hapticsEnabled = enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        preferences.notificationsEnabled = enabled
        _prefs.update { it.copy(notificationsEnabled = enabled) }
    }

    fun refreshFirebaseStatus() {
        viewModelScope.launch {
            connectionRepository.refreshConfigState()
        }
    }
}
