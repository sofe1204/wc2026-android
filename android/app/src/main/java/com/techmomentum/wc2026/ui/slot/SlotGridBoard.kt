package com.techmomentum.wc2026.ui.slot

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.techmomentum.wc2026.data.model.SlotCell
import com.techmomentum.wc2026.data.model.SlotGridPosition
import com.techmomentum.wc2026.ui.theme.CardGold

/** Lookup for slot symbol bitmaps pinned in memory, so cells paint with no flash. */
val LocalSlotBitmaps = staticCompositionLocalOf<(String) -> ImageBitmap?> { { null } }

@Composable
fun SlotGridBoard(
    cells: List<List<SlotCell>>,
    winningCells: Set<SlotGridPosition>,
    spinGeneration: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.aspectRatio(1f)) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                for (col in 0 until 3) {
                    val cell = cells.getOrNull(row)?.getOrNull(col)
                    key(spinGeneration, row, col, cell?.spinId) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(3.dp),
                        ) {
                            if (cell != null) {
                                SlotSymbolFace(
                                    cell = cell,
                                    isWinning = winningCells.contains(SlotGridPosition(row, col)),
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                SlotSymbolPlaceholder(
                                    label = "?",
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotSymbolFace(
    cell: SlotCell,
    isWinning: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "winPulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "winScale",
    )
    val context = LocalContext.current
    val imageUrl = cell.symbol.imageUrl
    val cacheKey = "${cell.spinId}|$imageUrl"
    val pinnedBitmap = LocalSlotBitmaps.current(imageUrl)
        ?.takeIf { it.width > 0 && it.height > 0 }
    val faceShape = RoundedCornerShape(10.dp)
    val defaultBorder = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.18f),
            CardGold.copy(alpha = 0.42f),
            Color(0xFF8B6914).copy(alpha = 0.55f),
        ),
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                if (isWinning) {
                    scaleX = pulse
                    scaleY = pulse
                }
            }
            .clip(faceShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3D5266), Color(0xFF1E2A36)),
                ),
            )
            .then(
                if (isWinning) {
                    Modifier.border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFFF8DC), CardGold, Color.White, CardGold),
                        ),
                        shape = faceShape,
                    )
                } else {
                    Modifier.border(width = 1.25.dp, brush = defaultBorder, shape = faceShape)
                },
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        val imageModifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))

        when {
            pinnedBitmap != null -> {
                Image(
                    bitmap = pinnedBitmap,
                    contentDescription = cell.symbol.label,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop,
                )
            }
            imageUrl.isNotBlank() -> {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false)
                        .memoryCacheKey(cacheKey)
                        .diskCacheKey(cacheKey)
                        .crossfade(false)
                        .build(),
                    contentDescription = cell.symbol.label,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop,
                    loading = {
                        SlotSymbolPlaceholder(label = "…", modifier = Modifier.fillMaxSize())
                    },
                    error = {
                        SlotSymbolPlaceholder(
                            label = cell.spinId.takeLast(6).ifBlank { "?" },
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                    success = {
                        SubcomposeAsyncImageContent(modifier = Modifier.fillMaxSize())
                    },
                )
            }
            else -> {
                SlotSymbolPlaceholder(
                    label = cell.symbol.label.ifBlank { cell.spinId.takeLast(6) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun SlotSymbolPlaceholder(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4A5F73), Color(0xFF2A3848)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.take(12),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            modifier = Modifier.padding(6.dp),
        )
    }
}
