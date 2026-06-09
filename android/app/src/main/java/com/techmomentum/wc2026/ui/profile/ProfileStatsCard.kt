package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.ui.components.AlbumProgressBar
import com.techmomentum.wc2026.ui.components.CollectorMetricRow
import com.techmomentum.wc2026.ui.components.CollectorPanel
import com.techmomentum.wc2026.ui.components.CollectorSectionTitle
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import kotlin.math.roundToInt

@Composable
fun ProfileStatsCard(
    profile: UserProfile,
    totalCollectible: Int,
    modifier: Modifier = Modifier,
) {
    val albumPercent = if (totalCollectible > 0) {
        profile.albumUniqueCount * 100f / totalCollectible
    } else {
        0f
    }
    val percentLabel = if (albumPercent < 0.1f) "0" else albumPercent.roundToInt().toString()

    CollectorPanel(modifier = modifier) {
        CollectorSectionTitle(title = "Your collection", trailing = "$percentLabel%")
        AlbumProgressBar(
            label = "Album completion",
            progress = albumPercent,
            detail = "${profile.albumUniqueCount} of $totalCollectible unique stickers",
            fillBrush = AlbumPageStyle.overallProgressFill,
            trackColor = AlbumPageStyle.filterUnselectedBorder.copy(alpha = 0.7f),
            barHeight = 10.dp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CollectorMetricRow(
                emoji = "📦",
                label = "Unopened packs",
                value = "${profile.unopenedPacks}",
                highlight = profile.unopenedPacks > 0,
            )
            CollectorMetricRow(
                emoji = "⭐",
                label = "Unique stickers",
                value = "${profile.albumUniqueCount}",
                highlight = true,
            )
            CollectorMetricRow(
                emoji = "🎴",
                label = "Total collected",
                value = "${profile.totalStickerCount}",
            )
            CollectorMetricRow(
                emoji = "🎰",
                label = "Slot spins left",
                value = "${profile.slotSpinsRemaining}",
                highlight = profile.slotSpinsRemaining > 0,
            )
        }
    }
}
