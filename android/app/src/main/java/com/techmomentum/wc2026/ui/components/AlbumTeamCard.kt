package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.domain.usecase.TeamAlbumProgress
import com.techmomentum.wc2026.ui.team.collectibleShadow
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.teamPalette

private val cardShape = RoundedCornerShape(20.dp)

@Composable
fun AlbumTeamCard(
    progress: TeamAlbumProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val team = progress.team
    val palette = teamPalette(team)
    val complete = progress.ownedCount >= progress.total && progress.total > 0
    val shadowColor = if (complete) CardGold else palette.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .collectibleShadow(
                color = shadowColor,
                elevation = if (complete) 11.dp else 9.dp,
                shape = cardShape,
            )
            .clip(cardShape)
            .border(
                width = if (complete) 2.dp else 0.dp,
                brush = Brush.linearGradient(listOf(CardGold, AnimePink, CardGold)),
                shape = cardShape,
            )
            .background(Brush.verticalGradient(palette.cardGradient))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TeamFlagBadge(
            team = team,
            palette = palette,
            size = 46.dp,
            showGlow = true,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = team.countryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.primary.darken(0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = team.group,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(palette.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.primary.darken(0.25f),
            )
            AlbumProgressBar(
                label = "Stickers",
                progress = progress.percent,
                detail = "${progress.ownedCount} / ${progress.total}",
                palette = palette,
            )
        }

        TeamProgressRing(
            progress = progress.percent / 100f,
            ownedCount = progress.ownedCount,
            total = progress.total,
            palette = palette,
            centerLabelColor = palette.primary.darken(0.35f),
        )
    }
}
