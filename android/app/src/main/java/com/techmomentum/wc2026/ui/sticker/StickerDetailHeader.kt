package com.techmomentum.wc2026.ui.sticker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.data.model.isTeamEmblem
import com.techmomentum.wc2026.ui.components.TeamFlagBadge
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken

private val detailFlagSize = 52.dp

/**
 * Compact banner: country flag, title + meta, sticker number / crest badge.
 */
@Composable
fun StickerDetailHeader(
    team: Team,
    player: Player?,
    sticker: Sticker,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    val isEmblem = sticker.isTeamEmblem()
    val title = when {
        isEmblem -> "Team emblem"
        else -> player?.playerName ?: sticker.stickerId
    }
    val meta = when {
        isEmblem -> buildString {
            append(team.countryName)
            sticker.group.takeIf { it.isNotBlank() }?.let { append(" • ").append(it) }
        }
        else -> buildString {
            append(team.countryName)
            sticker.group.takeIf { it.isNotBlank() }?.let { append(" • ").append(it) }
        }
    }
    val badgeLabel = if (isEmblem) "Crest" else "#${sticker.stickerNumber}"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = palette.primary.copy(alpha = 0.45f),
                spotColor = palette.primary.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(palette.heroGradient)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TeamFlagBadge(
                team = team,
                palette = palette,
                size = detailFlagSize,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = palette.onGradient,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.onGradient.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = badgeLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(palette.primary, palette.primary.darken(0.18f)),
                        ),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
