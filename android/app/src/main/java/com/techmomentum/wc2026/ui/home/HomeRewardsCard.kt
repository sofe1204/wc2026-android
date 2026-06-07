package com.techmomentum.wc2026.ui.home

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken

@Composable
fun HomeRewardsCard(
    loginPackAvailable: Boolean,
    adStickerAvailable: Boolean,
    adStickerCooldownMinutes: Int,
    slotSpinsRemaining: Int,
    slotPacksWonToday: Int,
    slotPackCap: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.18f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .border(1.dp, AlbumPageStyle.filterUnselectedBorder, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Daily rewards",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent.darken(0.1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeRewardTile(
                emoji = "🎁",
                title = "Login pack",
                status = if (loginPackAvailable) "Ready" else "Every 24h",
                available = loginPackAvailable,
                modifier = Modifier.weight(1f),
            )
            HomeRewardTile(
                emoji = "📺",
                title = "Ad stickers",
                status = if (adStickerAvailable) "Ready" else "${adStickerCooldownMinutes}m",
                available = adStickerAvailable,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeRewardTile(
                emoji = "🎰",
                title = "Slot spins",
                status = "$slotSpinsRemaining left",
                available = slotSpinsRemaining > 0,
                modifier = Modifier.weight(1f),
            )
            HomeRewardTile(
                emoji = "🏆",
                title = "Slot packs",
                status = "$slotPacksWonToday / $slotPackCap",
                available = slotPacksWonToday < slotPackCap,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HomeRewardTile(
    emoji: String,
    title: String,
    status: String,
    available: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                if (available) {
                    Brush.verticalGradient(
                        listOf(
                            AlbumPageStyle.headerAccentVivid,
                            AlbumPageStyle.headerAccent,
                        ),
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            AlbumPageStyle.filterUnselectedFill,
                            AlbumPageStyle.filterUnselectedFill,
                        ),
                    )
                },
            )
            .border(
                width = 1.dp,
                color = if (available) {
                    Color.Transparent
                } else {
                    AlbumPageStyle.filterUnselectedBorder
                },
                shape = shape,
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (available) Color.White else AlbumPageStyle.bottomNavUnselectedLabel,
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (available) {
                Color.White.copy(alpha = 0.88f)
            } else {
                AlbumPageStyle.bottomNavUnselectedIcon
            },
        )
    }
}
