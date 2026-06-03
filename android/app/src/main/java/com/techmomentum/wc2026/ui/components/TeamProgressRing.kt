package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.TeamPalette

@Composable
fun TeamProgressRing(
    progress: Float,
    ownedCount: Int,
    total: Int,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    centerLabelColor: Color = palette.primary,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = (size.value * 0.11f).dp.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = palette.primary.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (clamped > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(palette.accentVivid, palette.primary, palette.accentVivid),
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * clamped,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$ownedCount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = centerLabelColor,
            )
            Text(
                text = "/$total",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = centerLabelColor.copy(alpha = 0.75f),
            )
        }
    }
}
