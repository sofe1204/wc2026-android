package com.techmomentum.wc2026.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

private val buttonShape = RoundedCornerShape(22.dp)
private val highlightColor = Color(0xFFFFD54F)

@Composable
fun HomeOpenPackButton(
    unopenedPacks: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    highlighted: Boolean = false,
) {
    val hasPacks = unopenedPacks > 0
    if (!hasPacks) {
        HomeOpenPackEmptyState(modifier = modifier)
        return
    }

    val interactionSource = remember { MutableInteractionSource() }
    val animateIdle = enabled && !highlighted

    val infiniteTransition = rememberInfiniteTransition(label = "openPack")
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "idleScale",
    )
    val highlightPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "highlightPulse",
    )
    val highlightScale by infiniteTransition.animateFloat(
        initialValue = 1.06f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "highlightScale",
    )

    val scale = when {
        highlighted -> highlightScale
        animateIdle -> idleScale
        else -> 1f
    }

    val brush = when {
        highlighted -> Brush.verticalGradient(
            listOf(
                highlightColor.copy(alpha = 0.35f),
                AlbumPageStyle.filterSelectedStart.copy(alpha = 0.9f),
                AlbumPageStyle.filterSelectedEnd.copy(alpha = 0.9f),
            ),
        )
        else -> AlbumPageStyle.bottomNavSelectedBrush
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center,
    ) {
        if (highlighted) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .shadow(
                        elevation = (14.dp * highlightPulse),
                        shape = buttonShape,
                        ambientColor = highlightColor,
                        spotColor = highlightColor,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .size(112.dp)
                .shadow(
                    elevation = when {
                        highlighted -> 12.dp
                        enabled -> 8.dp
                        else -> 2.dp
                    },
                    shape = buttonShape,
                    ambientColor = if (highlighted) highlightColor else AlbumPageStyle.filterSelectedStart.copy(alpha = 0.35f),
                )
                .clip(buttonShape)
                .background(brush)
                .border(
                    width = if (highlighted) 6.dp else 0.dp,
                    color = highlightColor.copy(alpha = highlightPulse),
                    shape = buttonShape,
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "📦", fontSize = 32.sp)
                Text(
                    text = "Open pack",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF6B6B))
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (unopenedPacks > 9) "9+" else "$unopenedPacks",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun HomeOpenPackEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 112.dp, max = 200.dp)
                .clip(buttonShape)
                .background(AlbumPageStyle.filterUnselectedFill)
                .border(1.dp, AlbumPageStyle.filterUnselectedBorder, buttonShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = "📦", fontSize = 28.sp, color = AlbumPageStyle.bottomNavUnselectedIcon.copy(alpha = 0.65f))
            Text(
                text = "No packs ready",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AlbumPageStyle.headerAccent,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Watch an ad below for stickers",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = AlbumPageStyle.bottomNavUnselectedIcon,
                textAlign = TextAlign.Center,
            )
        }
    }
}
