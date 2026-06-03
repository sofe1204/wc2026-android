package com.techmomentum.wc2026.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.lighten
import kotlinx.coroutines.delay

@Composable
fun AnimatedStatBar(
    label: String,
    value: Int,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
    maxValue: Int = 99,
    animationDelayMs: Int = 0,
    barHeight: Dp = 10.dp,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(label, value) {
        started = false
        delay(animationDelayMs.toLong())
        started = true
    }

    val target = if (started) (value.toFloat() / maxValue.coerceAtLeast(1)).coerceIn(0f, 1f) else 0f
    val fraction by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "statBar",
    )

    val fillBrush = Brush.horizontalGradient(
        listOf(palette.primary, palette.accentVivid.lighten(0.12f)),
    )
    val trackColor = palette.primary.copy(alpha = 0.14f)
    val labelColor = palette.primary.darken(0.35f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = labelColor,
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = labelColor,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(50))
                .background(trackColor),
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(fillBrush),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                ),
                            ),
                    )
                }
            }
        }
    }
}
