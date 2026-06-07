package com.techmomentum.wc2026.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.components.WorldCupTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            WorldCupTopBar(title = "Settings", showBack = true, onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Preferences", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                SettingRow("Sound effects", state.soundEnabled) { viewModel.setSound(it) }
                SettingRow("Haptic feedback", state.hapticsEnabled) { viewModel.setHaptics(it) }
                SettingRow("Notifications", state.notificationsEnabled) { viewModel.setNotifications(it) }
            }
            Text(
                "Firebase connection",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.firebaseSummary, style = MaterialTheme.typography.bodyMedium)
                    if (state.firebaseProjectId.isNotBlank()) {
                        Text(
                            "Project: ${state.firebaseProjectId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        if (state.useFirebaseEmulators) {
                            "⚠ Local Firebase emulators (10.0.2.2) — run firebase emulators:start"
                        } else {
                            "✓ Production Firebase (google servers)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.useFirebaseEmulators) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                    if (state.isGuest) {
                        Text(
                            "Guest mode uses Firebase catalog; sign in to save progress and rewards in the cloud.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            Text(
                "Legal",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "TODO: Review World Cup 2026 branding, squad data, and sticker image licensing before release.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
