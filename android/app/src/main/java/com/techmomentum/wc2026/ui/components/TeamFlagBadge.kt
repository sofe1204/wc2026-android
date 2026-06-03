package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    showGlow: Boolean = true,
) {
    val flag = team.flagEmoji.ifBlank { team.teamCode }
    Box(
        modifier = modifier
            .then(
                if (showGlow) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = palette.accent.copy(alpha = 0.55f),
                        spotColor = palette.accentVivid.copy(alpha = 0.55f),
                    )
                } else {
                    Modifier
                },
            )
            .size(size + if (showGlow) 8.dp else 0.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.48f), Color.White.copy(alpha = 0.12f)),
                ),
            )
            .border(2.5.dp, Color.White.copy(alpha = 0.92f), CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = flag,
            fontSize = (size.value * 0.52f).sp,
        )
    }
}
