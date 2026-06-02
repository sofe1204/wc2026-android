package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.BuildConfig
import com.techmomentum.wc2026.ui.components.WorldCupTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isGuest: Boolean,
    onSignedOut: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        topBar = {
            WorldCupTopBar(
                title = "Profile",
                subtitle = if (isGuest) "Guest account" else "Collector account",
                showSettings = true,
                onSettings = onSettings,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(viewModel.displayName.ifBlank { "Collector" }, style = MaterialTheme.typography.headlineSmall)
                    Text(viewModel.email, style = MaterialTheme.typography.bodyMedium)
                    if (isGuest) {
                        Text(
                            "Offline demo — progress stored on device only.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            profile?.let { p ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Collection stats", style = MaterialTheme.typography.titleSmall)
                        Text("Unopened packs: ${p.unopenedPacks}")
                        Text("Unique stickers: ${p.albumUniqueCount}")
                        Text("Total collected (incl. dupes): ${p.totalStickerCount}")
                        Text("Slot spins: ${p.slotSpinsRemaining}")
                    }
                }
            }
            ui.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (BuildConfig.DEBUG && !isGuest) {
                OutlinedButton(
                    onClick = viewModel::seedFirestore,
                    enabled = !ui.seeding,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (ui.seeding) CircularProgressIndicator() else Text("Seed Firestore (admin)")
                }
            }
            OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                Text("App settings")
            }
            Button(
                onClick = {
                    viewModel.signOut()
                    onSignedOut()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Sign out") }
        }
    }
}
