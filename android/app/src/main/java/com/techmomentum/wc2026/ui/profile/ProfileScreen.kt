package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken

@Composable
fun ProfileScreen(
    isGuest: Boolean,
    onSignedOut: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsState()
    val totalCollectible by viewModel.totalCollectibleStickers.collectAsState()
    val subtitle = if (isGuest) "Guest account" else "Collector account"

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),
        ) {
            AlbumOverviewBackground()

            AlbumPageFrame(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ProfileOverviewHeader(
                        subtitle = subtitle,
                        onSettings = onSettings,
                    )

                    ProfileIdentityCard(
                        profile = profile,
                        email = viewModel.email,
                        isGuest = isGuest,
                    )

                    profile?.let { p ->
                        ProfileStatsCard(profile = p, totalCollectible = totalCollectible)
                    }

                    ProfileActionsCard(
                        onSettings = onSettings,
                        onSignOut = {
                            viewModel.signOut()
                            onSignedOut()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileActionsCard(
    onSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.15f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .border(1.dp, AlbumPageStyle.filterUnselectedBorder, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent.darken(0.1f),
        )
        PixarSecondaryButton(
            text = "App settings",
            onClick = onSettings,
            accentBorder = true,
        )
        PixarSecondaryButton(
            text = "Sign out",
            onClick = onSignOut,
        )
    }
}
