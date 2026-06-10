package com.techmomentum.wc2026.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

private val highlightColor = Color(0xFFFFD54F)

@Composable
fun PixarSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    accentBorder: Boolean = false,
    highlighted: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(16.dp)
    val highlightTransition = rememberInfiniteTransition(label = "secondaryHighlight")
    val highlightPulse by highlightTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "highlightPulse",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = when {
                    highlighted -> 10.dp
                    enabled -> 2.dp
                    else -> 0.dp
                },
                shape = shape,
                ambientColor = if (highlighted) highlightColor else Color.Black,
                spotColor = if (highlighted) highlightColor else Color.Black,
            )
            .clip(shape)
            .background(
                if (highlighted) {
                    highlightColor.copy(alpha = 0.22f)
                } else {
                    AlbumPageStyle.filterUnselectedFill
                },
            )
            .border(
                width = when {
                    highlighted -> 6.dp
                    accentBorder -> 2.dp
                    else -> 1.5.dp
                },
                color = when {
                    highlighted -> highlightColor.copy(alpha = highlightPulse)
                    accentBorder -> AlbumPageStyle.filterSelectedEnd.copy(alpha = if (enabled) 0.85f else 0.4f)
                    else -> AlbumPageStyle.filterUnselectedBorder.copy(alpha = if (enabled) 1f else 0.5f)
                },
                shape = shape,
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AlbumPageStyle.headerAccent,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AlbumPageStyle.bottomNavUnselectedLabel.copy(alpha = if (enabled) 1f else 0.45f),
            )
        }
    }
}
