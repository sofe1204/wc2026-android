package com.techmomentum.wc2026.ui.home

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.utils.GameConstants

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
    val subtitle = "Sticker album"
    val unopenedPacks = profile?.unopenedPacks ?: 0

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
                    HomeOverviewHeader(
                        welcomeName = welcomeName,
                        subtitle = subtitle,
                        onSettings = onSettings,
                    )

                    HomeDashboardCard(
                        unopenedPacks = unopenedPacks,
                        albumPercent = home.albumPercent,
                        uniqueCount = profile?.albumUniqueCount ?: 0,
                        totalCollected = profile?.totalStickerCount ?: 0,
                        slotSpins = profile?.slotSpinsRemaining ?: 0,
                        totalStickers = home.totalCollectibleStickers,
                    )

                    HomeRewardsCard(
                        loginPackAvailable = home.loginPackAvailable,
                        adStickerAvailable = home.adStickerAvailable,
                        adStickerCooldownMinutes = home.adStickerCooldownMinutes,
                        slotSpinsRemaining = profile?.slotSpinsRemaining ?: 0,
                        slotPacksWonToday = profile?.slotRewardPacksWonToday ?: 0,
                        slotPackCap = GameConstants.DAILY_SLOT_PACK_REWARD_CAP,
                    )

                    ui.message?.let { message ->
                        PixarCelebrationChip(message = message)
                    }

                    HomeActionsCard(
                        unopenedPacks = unopenedPacks,
                        loading = ui.loading,
                        adAvailable = home.adStickerAvailable,
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
                }
            }
        }
    }
}

@Composable
private fun HomeActionsCard(
    unopenedPacks: Int,
    loading: Boolean,
    adAvailable: Boolean,
    onOpenPack: () -> Unit,
    onWatchAd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.12f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .border(1.dp, AlbumPageStyle.filterUnselectedBorder, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent.darken(0.1f),
        )
        PixarPrimaryButton(
            text = if (unopenedPacks > 0) {
                "Open sticker pack ($unopenedPacks)"
            } else {
                "No packs to open"
            },
            onClick = onOpenPack,
            enabled = unopenedPacks > 0 && !loading,
        )
        PixarSecondaryButton(
            text = if (adAvailable) {
                "Watch ad · +${GameConstants.REWARDED_AD_STICKERS} stickers"
            } else {
                "Ad reward on cooldown"
            },
            onClick = onWatchAd,
            enabled = !loading && adAvailable,
            loading = loading && adAvailable,
            accentBorder = adAvailable,
        )
    }
}
