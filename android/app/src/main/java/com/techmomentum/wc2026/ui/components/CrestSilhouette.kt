package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/** Shield / crest placeholder for missing team emblem stickers. */
@Composable
fun CrestSilhouette(
    fill: Brush,
    modifier: Modifier = Modifier,
    outline: Color = Color.White.copy(alpha = 0.7f),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * 0.5f, h * 0.04f)
            cubicTo(w * 0.78f, h * 0.10f, w * 0.92f, h * 0.32f, w * 0.88f, h * 0.52f)
            cubicTo(w * 0.84f, h * 0.72f, w * 0.68f, h * 0.92f, w * 0.5f, h * 0.98f)
            cubicTo(w * 0.32f, h * 0.92f, w * 0.16f, h * 0.72f, w * 0.12f, h * 0.52f)
            cubicTo(w * 0.08f, h * 0.32f, w * 0.22f, h * 0.10f, w * 0.5f, h * 0.04f)
            close()
        }
        drawPath(path = shield, brush = fill, style = Fill)
        drawPath(
            path = shield,
            color = outline,
            style = Stroke(width = size.minDimension * 0.03f),
        )
    }
}
