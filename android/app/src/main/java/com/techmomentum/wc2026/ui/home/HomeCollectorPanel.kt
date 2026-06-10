package com.techmomentum.wc2026.ui.home



import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import com.techmomentum.wc2026.ui.components.AlbumProgressBar

import com.techmomentum.wc2026.ui.components.CollectorMetricRow

import com.techmomentum.wc2026.ui.components.CollectorPanel

import com.techmomentum.wc2026.ui.components.CollectorSectionTitle

import com.techmomentum.wc2026.ui.components.CollectorSoftDivider

import com.techmomentum.wc2026.ui.components.CollectorStatusRow

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

    totalStickers: Int,

    loginPackAvailable: Boolean,

    adStickerAvailable: Boolean,

    adStickerCooldownMinutes: Int,

    slotSpinsRemaining: Int,

    slotPacksWonToday: Int,

    slotSpinsAvailable: Boolean,

    slotSpinAdAvailable: Boolean,

    slotSpinAdCooldownMinutes: Int,

    loading: Boolean,

    highlightOpenPack: Boolean,

    highlightWatchAd: Boolean,

    onOpenPack: () -> Unit,

    onWatchAd: () -> Unit,

    onLoginPackTap: () -> Unit,

    onAdStickersTap: () -> Unit,

    onSlotSpinsTap: () -> Unit,

    openPackModifier: Modifier = Modifier,

    watchAdModifier: Modifier = Modifier,

    modifier: Modifier = Modifier,

) {

    val percentLabel = if (albumPercent < 0.1f) "0" else albumPercent.roundToInt().toString()

    val atSlotPackCap = slotPacksWonToday >= GameConstants.DAILY_SLOT_PACK_REWARD_CAP
    val slotSpinsStatus = when {
        atSlotPackCap ->
            "${GameConstants.DAILY_SLOT_PACK_REWARD_CAP}/${GameConstants.DAILY_SLOT_PACK_REWARD_CAP} packs won today"
        slotSpinsRemaining > 0 -> "$slotSpinsRemaining spins remaining"
        slotSpinAdAvailable -> "Watch ad for +${GameConstants.REWARDED_SLOT_SPINS} spins"
        slotSpinAdCooldownMinutes > 0 -> "Ready in ${slotSpinAdCooldownMinutes}m"
        else -> "No spins left"
    }



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

            CollectorMetricRow(emoji = "🎴", label = "Total collected", value = "$totalCollected")

        }



        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Box(

                modifier = Modifier.fillMaxWidth(),

                contentAlignment = Alignment.Center,

            ) {

                HomeOpenPackButton(

                    unopenedPacks = unopenedPacks,

                    onClick = onOpenPack,

                    enabled = unopenedPacks > 0 && !loading,

                    highlighted = highlightOpenPack,

                    modifier = openPackModifier,

                )

            }

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

                highlighted = highlightWatchAd,

                modifier = watchAdModifier,

            )

        }



        CollectorSoftDivider()



        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            CollectorSectionTitle(title = "Daily rewards")

            CollectorStatusRow(

                emoji = "🎁",

                title = "Login pack",

                status = if (loginPackAvailable) "Ready to claim" else "Resets at midnight UTC",

                available = loginPackAvailable,

                onClick = onLoginPackTap,

            )

            CollectorStatusRow(

                emoji = "📺",

                title = "Ad stickers",

                status = if (adStickerAvailable) "Watch an ad" else "Ready in ${adStickerCooldownMinutes}m",

                available = adStickerAvailable,

                onClick = onAdStickersTap,

            )

            CollectorStatusRow(

                emoji = "🎰",

                title = "Slot spins",

                status = slotSpinsStatus,

                available = slotSpinsAvailable,

                onClick = onSlotSpinsTap,

            )

        }

    }

}

