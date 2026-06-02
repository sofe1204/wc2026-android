package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A clean, vector-style football jersey drawn entirely in Compose — no emoji.
 * Used as the premium placeholder for missing stickers and as a fallback art for
 * owned stickers without an image.
 */
@Composable
fun JerseySilhouette(
    fill: Brush,
    modifier: Modifier = Modifier,
    outline: Color = Color.White.copy(alpha = 0.7f),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val shirt = Path().apply {
            // Left collar
            moveTo(w * 0.40f, h * 0.12f)
            // Neckline dip across to the right collar
            cubicTo(w * 0.43f, h * 0.20f, w * 0.57f, h * 0.20f, w * 0.60f, h * 0.12f)
            // Right shoulder -> sleeve tip -> inner sleeve
            lineTo(w * 0.74f, h * 0.08f)
            lineTo(w * 0.98f, h * 0.30f)
            lineTo(w * 0.82f, h * 0.46f)
            lineTo(w * 0.76f, h * 0.38f)
            // Right body edge down to hem
            lineTo(w * 0.76f, h * 0.92f)
            lineTo(w * 0.24f, h * 0.92f)
            // Left body edge up
            lineTo(w * 0.24f, h * 0.38f)
            lineTo(w * 0.18f, h * 0.46f)
            lineTo(w * 0.02f, h * 0.30f)
            lineTo(w * 0.26f, h * 0.08f)
            close()
        }
        drawPath(path = shirt, brush = fill, style = Fill)
        drawPath(
            path = shirt,
            color = outline,
            style = Stroke(width = size.minDimension * 0.03f),
        )
    }
}
