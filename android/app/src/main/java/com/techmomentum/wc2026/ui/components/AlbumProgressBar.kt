package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun AlbumProgressBar(
    label: String,
    progress: Float,
    detail: String,
    modifier: Modifier = Modifier,
    palette: TeamPalette? = null,
    fillBrush: Brush? = null,
    trackColor: Color? = null,
    labelColor: Color? = null,
    barHeight: Dp = 9.dp,
) {
    val fraction = (progress / 100f).coerceIn(0f, 1f)
    val resolvedFill = fillBrush ?: palette?.let {
        Brush.horizontalGradient(
            listOf(it.primary, it.accentVivid.lighten(0.15f)),
        )
    }
    val resolvedTrack = trackColor ?: palette?.primary?.copy(alpha = 0.14f)
        ?: MaterialTheme.colorScheme.surfaceVariant
    val resolvedLabel = labelColor ?: palette?.primary?.darken(0.35f)
        ?: MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$label · $detail",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = resolvedLabel,
        )
        if (resolvedFill != null) {
            PixarProgressTrack(
                fraction = fraction,
                fillBrush = resolvedFill,
                trackColor = resolvedTrack,
                barHeight = barHeight,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun PixarProgressTrack(
    fraction: Float,
    fillBrush: Brush,
    trackColor: Color,
    barHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
                                listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
            }
        }
    }
}
