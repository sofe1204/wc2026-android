package com.techmomentum.wc2026.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import com.techmomentum.wc2026.ui.components.CollectorPanel
import com.techmomentum.wc2026.ui.components.CollectorSectionTitle
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.layout.AlbumPageScreen

@Composable
fun ProfileScreen(
    onSignedOut: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsState()
    val totalCollectible by viewModel.totalCollectibleStickers.collectAsState()
    val avatarUi by viewModel.avatarUiState.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.uploadAvatar(uri)
    }

    AlbumPageScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ProfileHeroCard(
                profile = profile,
                email = viewModel.email,
                avatarUploading = avatarUi.uploading,
                onChangePhoto = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onSettings = onSettings,
            )

            avatarUi.message?.let { message ->
                LaunchedEffect(message) {
                    delay(4_000)
                    viewModel.clearAvatarMessage()
                }
                PixarCelebrationChip(message = message)
            }

            profile?.let { p ->
                ProfileStatsCard(profile = p, totalCollectible = totalCollectible)
            }

            CollectorPanel {
                CollectorSectionTitle(title = "Account")
                PixarSecondaryButton(
                    text = "App settings",
                    onClick = onSettings,
                    accentBorder = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                PixarSecondaryButton(
                    text = "Sign out",
                    onClick = {
                        viewModel.signOut()
                        onSignedOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
