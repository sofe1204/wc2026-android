package com.techmomentum.wc2026.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.local.AppPreferences
import com.techmomentum.wc2026.notifications.NotificationCoordinator
import com.techmomentum.wc2026.notifications.NotificationPermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val needsNotificationPermission: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: AppPreferences,
    private val notificationCoordinator: NotificationCoordinator,
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
        if (enabled) {
            if (NotificationPermissionHelper.isRequired() &&
                !NotificationPermissionHelper.hasPermission(context)
            ) {
                _uiState.update { it.copy(needsNotificationPermission = true) }
                return
            }
            applyNotificationsEnabled(true)
        } else {
            preferences.notificationsEnabled = false
            _uiState.update { it.copy(notificationsEnabled = false, needsNotificationPermission = false) }
            viewModelScope.launch { notificationCoordinator.onNotificationsDisabled() }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(needsNotificationPermission = false) }
        if (granted) {
            applyNotificationsEnabled(true)
        } else {
            preferences.notificationsEnabled = false
            _uiState.update { it.copy(notificationsEnabled = false) }
        }
    }

    fun clearPermissionPrompt() {
        _uiState.update { it.copy(needsNotificationPermission = false) }
    }

    private fun applyNotificationsEnabled(enabled: Boolean) {
        preferences.notificationsEnabled = enabled
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch { notificationCoordinator.onNotificationsEnabled() }
    }
}
