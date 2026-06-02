package com.techmomentum.wc2026.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Soft, colored drop shadow that makes cards feel like physical collectibles. */
fun Modifier.collectibleShadow(
    color: Color,
    elevation: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(CountryAlbumLayout.slotCornerRadius),
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = color.copy(alpha = 0.5f),
    spotColor = color.copy(alpha = 0.6f),
)

/** Glossy highlight strip across the top of a card, giving a soft 3D sheen. */
@Composable
fun GlossOverlay(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CountryAlbumLayout.slotCornerRadius,
    intensity: Float = 0.25f,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CountryAlbumLayout.glossHeight)
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = intensity),
                        Color.White.copy(alpha = 0.0f),
                    ),
                ),
            ),
    )
}
