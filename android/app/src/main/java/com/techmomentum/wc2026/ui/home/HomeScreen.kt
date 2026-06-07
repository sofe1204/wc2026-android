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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.AlbumProgressBar
import com.techmomentum.wc2026.ui.components.PackCard
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.utils.GameConstants

@Composable
fun HomeScreen(
    onOpenPack: () -> Unit,
    onSettings: () -> Unit,
    isGuest: Boolean,
    rewardedAdManager: RewardedAdManager? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val home by viewModel.homeState.collectAsState()
    val ui by viewModel.uiState.collectAsState()
    val profile = home.profile
    val context = LocalContext.current
    val welcomeName = "Welcome, ${profile?.displayName?.ifBlank { profile.email } ?: "Collector"}"
    val subtitle = if (isGuest) "Guest · Sticker Album" else "Sticker Album"
    val unopenedPacks = profile?.unopenedPacks ?: 0

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
                    HomeOverviewHeader(
                        welcomeName = welcomeName,
                        subtitle = subtitle,
                        onSettings = onSettings,
                    )

                    PackCard(
                        count = unopenedPacks,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AlbumProgressBar(
                            label = "Album progress",
                            progress = home.albumPercent,
                            detail = "${profile?.albumUniqueCount ?: 0} / ${GameConstants.TOTAL_STICKERS} unique",
                            fillBrush = AlbumPageStyle.overallProgressFill,
                            trackColor = AlbumPageStyle.filterUnselectedBorder,
                        )
                    }

                    HomeRewardsCard(
                        dailyClaimedToday = home.dailyClaimedToday,
                        adPackClaimedToday = home.adPackClaimedToday,
                        slotSpinsRemaining = profile?.slotSpinsRemaining ?: 0,
                        slotPacksWonToday = profile?.slotRewardPacksWonToday ?: 0,
                        slotPackCap = GameConstants.DAILY_SLOT_PACK_REWARD_CAP,
                    )

                    ui.message?.let { message ->
                        PixarCelebrationChip(message = message)
                    }

                    PixarPrimaryButton(
                        text = "Open Sticker Pack",
                        onClick = onOpenPack,
                        enabled = unopenedPacks > 0 && !ui.loading,
                    )

                    PixarSecondaryButton(
                        text = "Claim Daily Packs (+${GameConstants.DAILY_FREE_PACKS})",
                        onClick = viewModel::claimDailyPacks,
                        enabled = !ui.loading && !home.dailyClaimedToday,
                        loading = ui.loading && !home.dailyClaimedToday,
                    )

                    PixarSecondaryButton(
                        text = "Watch Ad for Bonus Pack",
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null && rewardedAdManager != null) {
                                rewardedAdManager.show(
                                    activity,
                                    onReward = { viewModel.claimRewardedAdPack() },
                                    onDismiss = {},
                                )
                            } else {
                                viewModel.claimRewardedAdPack()
                            }
                        },
                        enabled = !ui.loading && !home.adPackClaimedToday,
                        loading = ui.loading && !home.adPackClaimedToday,
                        accentBorder = true,
                    )
                }
            }
        }
    }
}
