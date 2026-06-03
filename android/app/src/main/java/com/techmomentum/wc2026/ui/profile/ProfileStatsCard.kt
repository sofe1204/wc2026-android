package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.ui.components.AlbumProgressBar
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.utils.GameConstants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileStatsCard(
    profile: UserProfile,
    modifier: Modifier = Modifier,
) {
    val albumPercent = if (GameConstants.TOTAL_STICKERS > 0) {
        profile.albumUniqueCount * 100f / GameConstants.TOTAL_STICKERS
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.2f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Collection stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent.darken(0.1f),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfileStatChip(
                emoji = "📦",
                label = "Unopened packs",
                value = "${profile.unopenedPacks}",
            )
            ProfileStatChip(
                emoji = "⭐",
                label = "Unique stickers",
                value = "${profile.albumUniqueCount}",
            )
            ProfileStatChip(
                emoji = "🎴",
                label = "Total collected",
                value = "${profile.totalStickerCount}",
            )
            ProfileStatChip(
                emoji = "🎰",
                label = "Slot spins",
                value = "${profile.slotSpinsRemaining}",
            )
        }
        AlbumProgressBar(
            label = "Album completion",
            progress = albumPercent,
            detail = "${profile.albumUniqueCount} / ${GameConstants.TOTAL_STICKERS} unique",
            fillBrush = AlbumPageStyle.overallProgressFill,
            trackColor = AlbumPageStyle.filterUnselectedBorder,
        )
    }
}
