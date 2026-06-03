package com.techmomentum.wc2026.ui.sticker

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class AttributeStat(val label: String, val value: Int)

@Composable
fun PlayerAttributeRadarChart(
    stats: List<AttributeStat>,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
    maxValue: Int = 99,
) {
    if (stats.isEmpty()) return

    var started by remember(stats) { mutableStateOf(false) }
    LaunchedEffect(stats) {
        started = false
        kotlinx.coroutines.delay(120)
        started = true
    }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "radarScale",
    )

    val gridColor = palette.primary.copy(alpha = 0.18f)
    val fillColor = palette.primary.copy(alpha = 0.28f)
    val strokeColor = palette.primary.darken(0.1f)
    val labelColor = palette.primary.darken(0.35f)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        color = labelColor,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val n = stats.size
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) * 0.34f
            val angleStep = 2.0 * PI / n

            fun pointAt(index: Int, fraction: Float): Offset {
                val angle = -PI / 2.0 + index * angleStep
                val r = radius * fraction * scale
                return Offset(
                    center.x + r * cos(angle).toFloat(),
                    center.y + r * sin(angle).toFloat(),
                )
            }

            for (ring in listOf(0.33f, 0.66f, 1f)) {
                val ringPath = Path()
                stats.indices.forEach { i ->
                    val p = pointAt(i, ring)
                    if (i == 0) ringPath.moveTo(p.x, p.y) else ringPath.lineTo(p.x, p.y)
                }
                ringPath.close()
                drawPath(ringPath, gridColor, style = Stroke(width = 1.5f))
            }

            val dataPath = Path()
            stats.forEachIndexed { i, stat ->
                val frac = (stat.value.toFloat() / maxValue.coerceAtLeast(1)).coerceIn(0f, 1f)
                val p = pointAt(i, frac)
                if (i == 0) dataPath.moveTo(p.x, p.y) else dataPath.lineTo(p.x, p.y)
            }
            dataPath.close()
            drawPath(dataPath, fillColor)
            drawPath(dataPath, strokeColor, style = Stroke(width = 2.5f))

            stats.forEachIndexed { i, stat ->
                val labelPoint = pointAt(i, 1.12f)
                val layout = textMeasurer.measure(
                    text = "${stat.label}\n${stat.value}",
                    style = labelStyle,
                    maxLines = 2,
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        labelPoint.x - layout.size.width / 2f,
                        labelPoint.y - layout.size.height / 2f,
                    ),
                )
            }
        }
    }
}
