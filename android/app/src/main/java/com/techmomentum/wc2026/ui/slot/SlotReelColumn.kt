package com.techmomentum.wc2026.ui.slot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.ui.theme.CardGold
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

/** Lookup for slot symbol bitmaps pinned in memory, so cells paint with no flash. */
val LocalSlotBitmaps = staticCompositionLocalOf<(String) -> ImageBitmap?> { { null } }

const val SLOT_REEL_LAND_DURATION_MS = 1_050

private const val VISIBLE_SLOTS = 3
private const val SPIN_SYMBOLS_PER_TICK = 3.5f
private const val SPIN_TICK_MS = 280
private const val LANDING_LEAD_COUNT = 10

@Composable
fun SlotReelColumn(
    columnIndex: Int,
    finalSymbols: List<SlotSymbol?>,
    isColumnSpinning: Boolean,
    stopRequested: Boolean,
    symbolPool: List<SlotSymbol>,
    spinGeneration: Int,
    modifier: Modifier = Modifier,
    winningRows: Set<Int> = emptySet(),
) {
    val columnShape = RoundedCornerShape(14.dp)
    val frameBrush = Brush.linearGradient(
        listOf(
            Color(0xFFB8860B),
            CardGold,
            Color.White.copy(alpha = 0.9f),
            CardGold,
            Color(0xFF8B6914),
        ),
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .clip(columnShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A2530), Color(0xFF0F1720)),
                ),
            )
            .border(2.dp, frameBrush, columnShape)
            .padding(2.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF0A1018))
            .border(1.dp, Color.Black.copy(alpha = 0.55f), RoundedCornerShape(11.dp)),
    ) {
        val cellHeight = maxHeight / VISIBLE_SLOTS
        val cellHeightPx = with(LocalDensity.current) { cellHeight.toPx() }

        ColumnReelEngine(
            columnIndex = columnIndex,
            columnSlots = normalizeColumnSlots(finalSymbols),
            isColumnSpinning = isColumnSpinning,
            stopRequested = stopRequested,
            symbolPool = symbolPool,
            spinGeneration = spinGeneration,
            cellHeight = cellHeight,
            cellHeightPx = cellHeightPx,
            winningRows = winningRows,
        )
    }
}

@Composable
private fun ColumnReelEngine(
    columnIndex: Int,
    columnSlots: List<SlotSymbol?>,
    isColumnSpinning: Boolean,
    stopRequested: Boolean,
    symbolPool: List<SlotSymbol>,
    spinGeneration: Int,
    cellHeight: Dp,
    cellHeightPx: Float,
    winningRows: Set<Int>,
) {
    val reelPosition = remember { Animatable(0f) }
    val landPosition = remember { Animatable(0f) }
    var hasSettled by remember { mutableStateOf(true) }
    var isLanding by remember { mutableStateOf(false) }
    var landingSequence by remember { mutableStateOf<List<SlotSymbol>>(emptyList()) }
    var lastLandSignal by remember { mutableIntStateOf(-1) }

    val displaySymbols = remember(columnSlots, symbolPool, columnIndex) {
        resolvedDisplaySymbols(columnSlots, symbolPool, columnIndex)
    }

    LaunchedEffect(spinGeneration) {
        if (spinGeneration == 0) return@LaunchedEffect
        hasSettled = false
        isLanding = false
        lastLandSignal = -1
        landingSequence = emptyList()
        reelPosition.snapTo(reelPosition.value % symbolPool.size.coerceAtLeast(1))
    }

    LaunchedEffect(isColumnSpinning, stopRequested, isLanding) {
        if (!isColumnSpinning && !stopRequested && !isLanding) {
            hasSettled = true
        }
    }

    LaunchedEffect(isColumnSpinning, symbolPool, spinGeneration, stopRequested) {
        if (!isColumnSpinning || symbolPool.isEmpty() || spinGeneration == 0 || isLanding || stopRequested) {
            return@LaunchedEffect
        }
        while (true) {
            reelPosition.animateTo(
                targetValue = reelPosition.value + SPIN_SYMBOLS_PER_TICK,
                animationSpec = tween(durationMillis = SPIN_TICK_MS, easing = LinearEasing),
            )
        }
    }

    LaunchedEffect(stopRequested, spinGeneration) {
        if (stopRequested && spinGeneration > 0) {
            hasSettled = false
        }
    }

    LaunchedEffect(stopRequested, spinGeneration, columnSlots, symbolPool) {
        if (!stopRequested || symbolPool.isEmpty()) return@LaunchedEffect

        val landSignal = spinGeneration * 10 + columnIndex
        if (lastLandSignal == landSignal) return@LaunchedEffect
        lastLandSignal = landSignal

        val finalsForLanding = landingFinals(columnSlots, symbolPool, columnIndex)
        isLanding = true
        landingSequence = buildLandingSequence(symbolPool, finalsForLanding)
        val landTarget = (landingSequence.size - VISIBLE_SLOTS).toFloat().coerceAtLeast(0f)

        landPosition.snapTo(0f)
        landPosition.animateTo(
            targetValue = landTarget,
            animationSpec = tween(durationMillis = SLOT_REEL_LAND_DURATION_MS, easing = FastOutSlowInEasing),
        )
        isLanding = false
        hasSettled = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp)),
    ) {
        when {
            isLanding && landingSequence.isNotEmpty() -> {
                SequenceReelViewport(
                    sequence = landingSequence,
                    position = landPosition.value,
                    columnIndex = columnIndex,
                    cellHeight = cellHeight,
                    cellHeightPx = cellHeightPx,
                )
            }
            // Checked before the spinning branches so a finished column stays settled even
            // while the global phase is still "Settling" (prevents a last-column flicker).
            hasSettled -> {
                SettledColumnView(
                    symbols = displaySymbols,
                    columnIndex = columnIndex,
                    winningRows = winningRows,
                )
            }
            (isColumnSpinning || stopRequested) && symbolPool.isNotEmpty() -> {
                ModuloReelViewport(
                    pool = symbolPool,
                    position = reelPosition.value,
                    columnIndex = columnIndex,
                    cellHeight = cellHeight,
                    cellHeightPx = cellHeightPx,
                )
            }
            else -> {
                SettledColumnView(
                    symbols = displaySymbols,
                    columnIndex = columnIndex,
                    winningRows = winningRows,
                )
            }
        }

        repeat(VISIBLE_SLOTS - 1) { dividerIndex ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = cellHeight * (dividerIndex + 1) - 1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                CardGold.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.35f),
                                CardGold.copy(alpha = 0.45f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun ModuloReelViewport(
    pool: List<SlotSymbol>,
    position: Float,
    columnIndex: Int,
    cellHeight: Dp,
    cellHeightPx: Float,
) {
    val baseIndex = floor(position).toInt()
    val fraction = position - baseIndex
    val offsetPx = fraction * cellHeightPx
    val poolSize = pool.size

    ScrollingSymbolsColumn(
        symbols = (-1 until VISIBLE_SLOTS + 1).map { offset ->
            pool[poolIndex(baseIndex + offset, poolSize)]
        },
        columnIndex = columnIndex,
        cellHeight = cellHeight,
        offsetPx = offsetPx,
    )
}

@Composable
private fun SequenceReelViewport(
    sequence: List<SlotSymbol>,
    position: Float,
    columnIndex: Int,
    cellHeight: Dp,
    cellHeightPx: Float,
) {
    val baseIndex = floor(position).toInt().coerceIn(0, (sequence.size - 1).coerceAtLeast(0))
    val fraction = (position - floor(position)).coerceIn(0f, 1f)
    val offsetPx = fraction * cellHeightPx

    ScrollingSymbolsColumn(
        symbols = (-1 until VISIBLE_SLOTS + 1).map { offset ->
            sequence.getOrElse(baseIndex + offset) { sequence.last() }
        },
        columnIndex = columnIndex,
        cellHeight = cellHeight,
        offsetPx = offsetPx,
    )
}

@Composable
private fun ScrollingSymbolsColumn(
    symbols: List<SlotSymbol>,
    columnIndex: Int,
    cellHeight: Dp,
    offsetPx: Float,
) {
    Column(
        modifier = Modifier.offset { IntOffset(0, offsetPx.roundToInt()) },
    ) {
        symbols.forEachIndexed { index, symbol ->
            key(columnIndex, index, symbol.symbolId) {
                SlotSymbolFace(
                    symbol = symbol,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight),
                )
            }
        }
    }
}

@Composable
private fun SettledColumnView(
    symbols: List<SlotSymbol>,
    columnIndex: Int,
    winningRows: Set<Int>,
) {
    val transition = rememberInfiniteTransition(label = "winPulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "winScale",
    )
    val hasWin = winningRows.isNotEmpty()

    Column(modifier = Modifier.fillMaxHeight()) {
        repeat(VISIBLE_SLOTS) { index ->
            val symbol = symbols.getOrElse(index) { symbols.first() }
            val isWinning = index in winningRows
            key(columnIndex, index, symbol.symbolId) {
                SlotSymbolFace(
                    symbol = symbol,
                    highlight = isWinning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            if (isWinning) {
                                scaleX = pulse
                                scaleY = pulse
                            }
                            alpha = if (hasWin && !isWinning) 0.45f else 1f
                        },
                )
            }
        }
    }
}

@Composable
private fun SlotSymbolFace(
    symbol: SlotSymbol,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val context = LocalContext.current
    val pinnedBitmap = LocalSlotBitmaps.current(symbol.imageUrl)
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
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(faceShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3D5266), Color(0xFF1E2A36)),
                ),
            )
            .then(
                if (highlight) {
                    Modifier.border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFFFFF8DC),
                                CardGold,
                                Color.White,
                                CardGold,
                            ),
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
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
            )

        when {
            pinnedBitmap != null -> {
                Image(
                    bitmap = pinnedBitmap,
                    contentDescription = symbol.label,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop,
                )
            }
            symbol.imageUrl.isNotBlank() -> {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(symbol.imageUrl)
                        .allowHardware(false)
                        .memoryCacheKey(symbol.imageUrl)
                        .diskCacheKey(symbol.imageUrl)
                        .crossfade(false)
                        .build(),
                    contentDescription = symbol.label,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop,
                    loading = { SlotSymbolPlaceholder(label = symbol.label) },
                    error = { SlotSymbolPlaceholder(label = symbol.label) },
                    success = {
                        SubcomposeAsyncImageContent(modifier = Modifier.fillMaxSize())
                    },
                )
            }
            else -> {
                SlotSymbolPlaceholder(label = symbol.label)
            }
        }
    }
}

@Composable
private fun SlotSymbolPlaceholder(label: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.take(12),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                modifier = Modifier.padding(4.dp),
            )
        } else {
            Text(
                text = "?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

private fun normalizeColumnSlots(finalSymbols: List<SlotSymbol?>): List<SlotSymbol?> {
    return List(VISIBLE_SLOTS) { index -> finalSymbols.getOrNull(index) }
}

private fun resolvedDisplaySymbols(
    columnSlots: List<SlotSymbol?>,
    pool: List<SlotSymbol>,
    columnIndex: Int,
): List<SlotSymbol> = landingFinals(columnSlots, pool, columnIndex)

private fun landingFinals(
    columnSlots: List<SlotSymbol?>,
    pool: List<SlotSymbol>,
    columnIndex: Int,
): List<SlotSymbol> {
    if (pool.isEmpty()) {
        return List(VISIBLE_SLOTS) { index ->
            columnSlots.getOrNull(index) ?: fallbackSymbol(columnIndex, index)
        }
    }
    return List(VISIBLE_SLOTS) { row ->
        columnSlots.getOrNull(row) ?: pool[poolIndex(row + columnIndex, pool.size)]
    }
}

private fun fallbackSymbol(columnIndex: Int, row: Int): SlotSymbol =
    SlotSymbol(
        symbolId = "missing_${columnIndex}_$row",
        label = "?",
    )

private fun buildLandingSequence(pool: List<SlotSymbol>, finals: List<SlotSymbol>): List<SlotSymbol> {
    val lead = List(LANDING_LEAD_COUNT) { pool[Random.nextInt(pool.size)] }
    return lead + finals
}

private fun poolIndex(index: Int, poolSize: Int): Int {
    if (poolSize == 0) return 0
    return ((index % poolSize) + poolSize) % poolSize
}
