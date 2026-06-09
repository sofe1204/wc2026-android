package com.techmomentum.wc2026.ui.home

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.pack.AnimatedPackLoader
import com.techmomentum.wc2026.ui.pack.StickerRevealFlow
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.utils.GameConstants
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onOpenPack: () -> Unit,
    onSettings: () -> Unit,
    rewardedAdManager: RewardedAdManager? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val home by viewModel.homeState.collectAsState()
    val ui by viewModel.uiState.collectAsState()
    val profile = home.profile
    val context = LocalContext.current
    val displayName = profile?.firstName?.takeIf { it.isNotBlank() }
        ?: profile?.displayName?.takeIf { it.isNotBlank() }
        ?: profile?.username?.takeIf { it.isNotBlank() }?.let { "@$it" }
        ?: "Collector"
    val welcomeName = "Hey, $displayName 👋"
    val unopenedPacks = profile?.unopenedPacks ?: 0

    LaunchedEffect(ui.message) {
        ui.message?.let {
            delay(4_000)
            viewModel.clearMessage()
        }
    }

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
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    HomeOverviewHeader(
                        welcomeName = welcomeName,
                        subtitle = "Your album",
                        onSettings = onSettings,
                    )

                    HomeCollectorPanel(
                        unopenedPacks = unopenedPacks,
                        albumPercent = home.albumPercent,
                        uniqueCount = profile?.albumUniqueCount ?: 0,
                        totalCollected = profile?.totalStickerCount ?: 0,
                        slotSpins = profile?.slotSpinsRemaining ?: 0,
                        totalStickers = home.totalCollectibleStickers,
                        loginPackAvailable = home.loginPackAvailable,
                        adStickerAvailable = home.adStickerAvailable,
                        adStickerCooldownMinutes = home.adStickerCooldownMinutes,
                        slotSpinsRemaining = profile?.slotSpinsRemaining ?: 0,
                        slotPacksWonToday = profile?.slotRewardPacksWonToday ?: 0,
                        slotPackCap = GameConstants.DAILY_SLOT_PACK_REWARD_CAP,
                        loading = ui.loading,
                        onOpenPack = onOpenPack,
                        onWatchAd = {
                            val activity = context as? Activity
                            if (activity != null && rewardedAdManager != null) {
                                rewardedAdManager.show(
                                    activity,
                                    onReward = { viewModel.claimRewardedAdStickers() },
                                    onDismiss = {},
                                )
                            } else {
                                viewModel.claimRewardedAdStickers()
                            }
                        },
                    )

                    ui.message?.let { message ->
                        PixarCelebrationChip(message = message)
                    }
                }
            }

            if (ui.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        AnimatedPackLoader(modifier = Modifier.fillMaxWidth(0.7f))
                        Text(
                            text = "Claiming your stickers…",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }

            if (ui.showStickerReveal && ui.revealed.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),
                ) {
                    AlbumOverviewBackground()
                    AlbumPageFrame(modifier = Modifier.fillMaxSize()) {
                        StickerRevealFlow(
                            revealed = ui.revealed,
                            revealIndex = ui.revealIndex,
                            title = "📺 Ad reward stickers!",
                            doneLabel = "Add to Album",
                            onRevealNext = viewModel::revealNextSticker,
                            onDone = viewModel::dismissStickerReveal,
                        )
                    }
                }
            }
        }
    }
}
