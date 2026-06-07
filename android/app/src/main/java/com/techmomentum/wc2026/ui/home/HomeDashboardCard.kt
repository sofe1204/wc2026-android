package com.techmomentum.wc2026.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.techmomentum.wc2026.ui.components.AlbumProgressBar
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken
import kotlin.math.roundToInt

@Composable
fun HomeDashboardCard(
    unopenedPacks: Int,
    albumPercent: Float,
    uniqueCount: Int,
    totalCollected: Int,
    slotSpins: Int,
    totalStickers: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val percentLabel = if (albumPercent < 0.1f) "0" else albumPercent.roundToInt().toString()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.22f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .border(1.dp, AlbumPageStyle.filterUnselectedBorder, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(108.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AlbumPageStyle.headerAccentVivid,
                                AlbumPageStyle.headerAccent,
                                AlbumPageStyle.headerAccent.darken(0.08f),
                            ),
                        ),
                    )
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📦", fontSize = 26.sp)
                    Text(
                        text = "$unopenedPacks",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    Text(
                        text = if (unopenedPacks == 1) "pack" else "packs",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Album",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AlbumPageStyle.headerAccent.darken(0.1f),
                    )
                    Text(
                        text = "$percentLabel%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = AlbumPageStyle.headerAccent,
                    )
                }
                AlbumProgressBar(
                    label = "",
                    progress = albumPercent,
                    detail = "$uniqueCount / $totalStickers unique",
                    fillBrush = AlbumPageStyle.overallProgressFill,
                    trackColor = AlbumPageStyle.filterUnselectedBorder,
                    barHeight = 12.dp,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeMiniStat(
                emoji = "⭐",
                value = "$uniqueCount",
                label = "Unique",
                modifier = Modifier.weight(1f),
            )
            HomeMiniStat(
                emoji = "🎴",
                value = "$totalCollected",
                label = "Collected",
                modifier = Modifier.weight(1f),
            )
            HomeMiniStat(
                emoji = "🎰",
                value = "$slotSpins",
                label = "Spins",
                modifier = Modifier.weight(1f),
                highlight = slotSpins > 0,
            )
        }
    }
}

@Composable
private fun HomeMiniStat(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                if (highlight) {
                    Brush.verticalGradient(
                        listOf(
                            AlbumPageStyle.headerAccentVivid.copy(alpha = 0.14f),
                            AlbumPageStyle.headerAccent.copy(alpha = 0.08f),
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
                color = if (highlight) {
                    AlbumPageStyle.headerAccent.copy(alpha = 0.35f)
                } else {
                    AlbumPageStyle.filterUnselectedBorder
                },
                shape = shape,
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlbumPageStyle.bottomNavUnselectedIcon,
        )
    }
}
