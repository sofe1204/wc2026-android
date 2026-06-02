package com.techmomentum.wc2026.ui.team

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.domain.usecase.StickerSlot
import com.techmomentum.wc2026.ui.components.TeamEmblem
import com.techmomentum.wc2026.ui.components.rarityColor
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.lighten
import com.techmomentum.wc2026.ui.theme.teamPalette

/**
 * Compact, bright banner header. A short rounded card with the team gradient, emblem,
 * name + group, and a small progress ring — leaving most of the screen for the grid.
 */
@Composable
fun CountryAlbumHero(
    team: Team,
    ownedCount: Int,
    total: Int,
    percent: Float,
    modifier: Modifier = Modifier,
) {
    val palette = teamPalette(team)
    val progress = (percent / 100f).coerceIn(0f, 1f)
    val subtitle = when {
        ownedCount == 0 -> "Collect the full squad!"
        ownedCount >= total -> "Squad complete!"
        else -> "${total - ownedCount} stickers to go"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = palette.primary.copy(alpha = 0.5f),
                spotColor = palette.primary.copy(alpha = 0.5f),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(palette.heroGradient)),
    ) {
        // Soft glossy sheen across the top.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                    ),
                ),
        )
        // Subtle football watermark.
        Text(
            "⚽",
            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
            fontSize = 26.sp,
            color = palette.onGradient.copy(alpha = 0.16f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Emblem with soft glow + white ring.
            Box(
                modifier = Modifier
                    .size(CountryAlbumLayout.bannerEmblemSize + 12.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        ambientColor = palette.accent.copy(alpha = 0.6f),
                        spotColor = palette.accentVivid.copy(alpha = 0.6f),
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.12f)),
                        ),
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.95f), CircleShape)
                    .padding(5.dp),
                contentAlignment = Alignment.Center,
            ) {
                TeamEmblem(team = team, size = CountryAlbumLayout.bannerEmblemSize)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(team.flagEmoji, fontSize = 22.sp)
                    Text(
                        text = team.countryName,
                        modifier = Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = palette.onGradient,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = team.group,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(palette.accent, palette.accent.lighten(0.2f)),
                                ),
                            )
                            .padding(horizontal = 11.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.onGradient.copy(alpha = 0.92f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            HeroProgressRing(progress, ownedCount, total, palette)
        }
    }
}

@Composable
private fun HeroProgressRing(
    progress: Float,
    ownedCount: Int,
    total: Int,
    palette: TeamPalette,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(CountryAlbumLayout.heroProgressSize)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = Color.White.copy(alpha = 0.30f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(palette.accent, Color.White, palette.accent),
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$ownedCount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = palette.onGradient,
            )
            Text(
                text = "/$total",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = palette.onGradient.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
fun SquadSummaryStrip(
    slots: List<StickerSlot>,
    ownedCount: Int,
    total: Int,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    val ownedByRarity = slots.filter { it.owned != null }.groupingBy { it.sticker.rarity }.eachCount()
    val totalByRarity = slots.groupingBy { it.sticker.rarity }.eachCount()

    Column(
        modifier = modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "$ownedCount of $total collected",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = palette.primary.darken(0.35f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Rarity.entries.forEach { rarity ->
                val owned = ownedByRarity[rarity] ?: 0
                val count = totalByRarity[rarity] ?: 0
                if (count > 0) {
                    val color = rarityColor(rarity)
                    Text(
                        text = "$owned/$count ${rarity.name.lowercase()}",
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(50))
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(color, color.lighten(0.22f)),
                                ),
                            )
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
