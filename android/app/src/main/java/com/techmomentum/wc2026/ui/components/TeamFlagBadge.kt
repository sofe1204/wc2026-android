package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.ui.theme.TeamPalette

@Composable
fun TeamFlagBadge(
    team: Team,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val flag = team.flagEmoji.ifBlank { team.teamCode }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        palette.primary.copy(alpha = 0.14f),
                        palette.accent.copy(alpha = 0.06f),
                    ),
                ),
            )
            .border(1.5.dp, palette.primary.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = flag,
            fontSize = (size.value * 0.60f).sp,
        )
    }
}
