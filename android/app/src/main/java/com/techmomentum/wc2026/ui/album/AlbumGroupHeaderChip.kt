package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.techmomentum.wc2026.domain.usecase.TeamAlbumProgress
import androidx.compose.ui.graphics.luminance
import com.techmomentum.wc2026.ui.theme.lighten
import com.techmomentum.wc2026.ui.theme.teamPalette

@Composable
fun AlbumGroupHeaderChip(
    group: String,
    teams: List<TeamAlbumProgress>,
    modifier: Modifier = Modifier,
) {
    val palette = teamPalette(teams.first().team)
    val ownedInGroup = teams.sumOf { it.ownedCount }
    val totalInGroup = teams.sumOf { it.total }
    val onPill = if (palette.primary.luminance() > 0.55f) Color(0xFF1A1A1A) else Color.White

    Row(
        modifier = modifier
            .padding(top = 6.dp, bottom = 4.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(50),
                ambientColor = palette.primary.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(palette.primary, palette.primary.lighten(0.22f)),
                ),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = group,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = onPill,
        )
        Text(
            text = "$ownedInGroup / $totalInGroup",
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.22f))
                .padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = onPill,
        )
    }
}
