package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.BuildConfig
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun ProfileScreen(
    isGuest: Boolean,
    onSignedOut: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val displayName = viewModel.displayName.ifBlank { "Collector" }
    val subtitle = if (isGuest) "Guest account" else "Collector account"

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),
        ) {
            AlbumOverviewBackground()

            AlbumPageFrame(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ProfileOverviewHeader(
                        subtitle = subtitle,
                        displayName = displayName,
                        onSettings = onSettings,
                    )

                    ProfileIdentityCard(
                        displayName = displayName,
                        email = viewModel.email,
                        isGuest = isGuest,
                    )

                    profile?.let { p ->
                        ProfileStatsCard(profile = p)
                    }

                    ui.message?.let { message ->
                        PixarCelebrationChip(message = message)
                    }

                    PixarSecondaryButton(
                        text = "App settings",
                        onClick = onSettings,
                    )

                    PixarPrimaryButton(
                        text = "Sign out",
                        onClick = {
                            viewModel.signOut()
                            onSignedOut()
                        },
                    )

                    if (BuildConfig.DEBUG && !isGuest) {
                        PixarSecondaryButton(
                            text = "Seed Firestore (admin)",
                            onClick = viewModel::seedFirestore,
                            enabled = !ui.seeding,
                            loading = ui.seeding,
                        )
                    }
                }
            }
        }
    }
}
