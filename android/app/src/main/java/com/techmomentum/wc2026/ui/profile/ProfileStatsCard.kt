package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.2f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .border(1.dp, AlbumPageStyle.filterUnselectedBorder, RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Collection stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent.darken(0.1f),
            )
            Text(
                text = "$percentLabel%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileStatChip(
                    emoji = "📦",
                    label = "Unopened packs",
                    value = "${profile.unopenedPacks}",
                    modifier = Modifier.weight(1f),
                )
                ProfileStatChip(
                    emoji = "⭐",
                    label = "Unique stickers",
                    value = "${profile.albumUniqueCount}",
                    modifier = Modifier.weight(1f),
                    highlighted = true,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileStatChip(
                    emoji = "🎴",
                    label = "Total collected",
                    value = "${profile.totalStickerCount}",
                    modifier = Modifier.weight(1f),
                )
                ProfileStatChip(
                    emoji = "🎰",
                    label = "Slot spins left",
                    value = "${profile.slotSpinsRemaining}",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        AlbumProgressBar(
            label = "Album completion",
            progress = albumPercent,
            detail = "${profile.albumUniqueCount} / $totalCollectible unique stickers",
            fillBrush = AlbumPageStyle.overallProgressFill,
            trackColor = AlbumPageStyle.filterUnselectedBorder,
            barHeight = 12.dp,
        )
    }
}
