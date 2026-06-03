package com.techmomentum.wc2026.ui.sticker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken

@Composable
fun StickerDetailStatsUnavailable(
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Detailed attributes are not available for this player yet.",
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = palette.primary.copy(alpha = 0.25f),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.88f))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = palette.primary.darken(0.3f),
        textAlign = TextAlign.Center,
    )
}
