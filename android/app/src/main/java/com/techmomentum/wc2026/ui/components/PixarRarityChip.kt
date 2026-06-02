package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.ui.theme.lighten

@Composable
fun PixarRarityChip(
    rarity: Rarity,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
) {
    val color = rarityColor(rarity)
    val label = rarity.name.lowercase().replaceFirstChar { it.uppercase() }
    val textColor = if (color.luminance() > 0.6f) Color(0xFF1A1A1A) else Color.White
    Text(
        text = label,
        modifier = modifier
            .shadow(if (muted) 0.dp else 3.dp, RoundedCornerShape(50), ambientColor = color)
            .clip(RoundedCornerShape(50))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (muted) color.copy(alpha = 0.4f) else color,
                        if (muted) color.copy(alpha = 0.55f) else color.lighten(0.25f),
                    ),
                ),
                shape = RoundedCornerShape(50),
            )
            .padding(horizontal = 9.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
        ),
        color = textColor.copy(alpha = if (muted) 0.85f else 1f),
    )
}
