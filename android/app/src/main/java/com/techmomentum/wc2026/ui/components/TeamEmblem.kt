package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.ui.theme.parseTeamColor

@Composable
fun TeamEmblem(
    team: Team,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    if (team.customEmblemUrl.isNotBlank()) {
        AsyncImage(
            model = team.customEmblemUrl,
            contentDescription = "${team.countryName} emblem",
            modifier = modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        val bg = parseTeamColor(team.primaryColor, MaterialTheme.colorScheme.primary)
        val fg = parseTeamColor(team.secondaryColor, Color.White)
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = team.teamCode.ifBlank { team.flagEmoji },
                color = fg,
                fontSize = (size.value * 0.28f).sp,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
