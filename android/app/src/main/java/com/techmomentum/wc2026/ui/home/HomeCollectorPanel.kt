package com.techmomentum.wc2026.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.components.AlbumProgressBar
import com.techmomentum.wc2026.ui.components.CollectorMetricRow
import com.techmomentum.wc2026.ui.components.CollectorPanel
import com.techmomentum.wc2026.ui.components.CollectorSectionTitle
import com.techmomentum.wc2026.ui.components.CollectorSoftDivider
import com.techmomentum.wc2026.ui.components.CollectorStatusRow
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.utils.GameConstants
import kotlin.math.roundToInt

@Composable
fun HomeCollectorPanel(
    unopenedPacks: Int,
    albumPercent: Float,
    uniqueCount: Int,
    totalCollected: Int,
    slotSpins: Int,
    totalStickers: Int,
    loginPackAvailable: Boolean,
    adStickerAvailable: Boolean,
    adStickerCooldownMinutes: Int,
    slotSpinsRemaining: Int,
    slotPacksWonToday: Int,
    slotPackCap: Int,
    loading: Boolean,
    onOpenPack: () -> Unit,
    onWatchAd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val percentLabel = if (albumPercent < 0.1f) "0" else albumPercent.roundToInt().toString()

    CollectorPanel(modifier = modifier) {
        CollectorSectionTitle(title = "Album progress", trailing = "$percentLabel%")
        AlbumProgressBar(
            label = "",
            progress = albumPercent,
            detail = "$uniqueCount of $totalStickers unique stickers",
            fillBrush = AlbumPageStyle.overallProgressFill,
            trackColor = AlbumPageStyle.filterUnselectedBorder.copy(alpha = 0.7f),
            barHeight = 10.dp,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CollectorMetricRow(
                emoji = "📦",
                label = "Packs ready to open",
                value = "$unopenedPacks",
                highlight = unopenedPacks > 0,
            )
            CollectorMetricRow(emoji = "⭐", label = "Unique stickers", value = "$uniqueCount", highlight = true)
            CollectorMetricRow(emoji = "🎴", label = "Total collected", value = "$totalCollected")
            CollectorMetricRow(
                emoji = "🎰",
                label = "Slot spins left",
                value = "$slotSpins",
                highlight = slotSpins > 0,
            )
        }

        CollectorSoftDivider()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CollectorSectionTitle(title = "Daily rewards")
            CollectorStatusRow(
                emoji = "🎁",
                title = "Login pack",
                status = if (loginPackAvailable) "Ready to claim" else "Every 24 hours",
                available = loginPackAvailable,
            )
            CollectorStatusRow(
                emoji = "📺",
                title = "Ad stickers",
                status = if (adStickerAvailable) "Watch an ad" else "Ready in ${adStickerCooldownMinutes}m",
                available = adStickerAvailable,
            )
            CollectorStatusRow(
                emoji = "🎰",
                title = "Slot spins",
                status = "$slotSpinsRemaining spins remaining",
                available = slotSpinsRemaining > 0,
            )
            CollectorStatusRow(
                emoji = "🏆",
                title = "Slot pack wins",
                status = "$slotPacksWonToday of $slotPackCap today",
                available = slotPacksWonToday < slotPackCap,
            )
        }

        CollectorSoftDivider()

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PixarPrimaryButton(
                text = if (unopenedPacks > 0) "Open sticker pack ($unopenedPacks)" else "No packs to open",
                onClick = onOpenPack,
                enabled = unopenedPacks > 0 && !loading,
            )
            PixarSecondaryButton(
                text = if (adStickerAvailable) {
                    "Watch ad · +${GameConstants.REWARDED_AD_STICKERS} stickers"
                } else {
                    "Ad reward on cooldown"
                },
                onClick = onWatchAd,
                enabled = !loading && adStickerAvailable,
                loading = loading && adStickerAvailable,
                accentBorder = adStickerAvailable,
            )
        }
    }
}
