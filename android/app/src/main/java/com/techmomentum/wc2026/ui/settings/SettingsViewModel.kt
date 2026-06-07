package com.techmomentum.wc2026.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.local.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            soundEnabled = preferences.soundEnabled,
            hapticsEnabled = preferences.hapticsEnabled,
            notificationsEnabled = preferences.notificationsEnabled,
        ),
    )

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setSound(enabled: Boolean) {
        preferences.soundEnabled = enabled
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        preferences.hapticsEnabled = enabled
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        preferences.notificationsEnabled = enabled
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }
}
