package com.techmomentum.wc2026.ui.slot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.data.model.SlotCell
import com.techmomentum.wc2026.data.model.SlotGridPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val COLUMN_STAGGER_MS = 180L
private const val CASCADE_FINISH_BUFFER_MS = 750L
private const val DROP_OFFSET_Y = -900f

@Composable
fun SlotCascadeBoard(
    cells: List<List<SlotCell>>,
    winningCells: Set<SlotGridPosition>,
    spinGeneration: Int,
    spinResultReady: Boolean,
    isWin: Boolean,
    onSpinStarted: () -> Unit,
    onColumnLanded: (Int) -> Unit,
    onSpinFinished: (Boolean) -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val columnOffsets = remember(spinGeneration) {
        List(3) { Animatable(DROP_OFFSET_Y) }
    }

    LaunchedEffect(spinGeneration, spinResultReady) {
        if (!spinResultReady) return@LaunchedEffect

        columnOffsets.forEach { it.snapTo(DROP_OFFSET_Y) }
        onSpinStarted()

        columnOffsets.forEachIndexed { col, animatable ->
            launch {
                delay(col * COLUMN_STAGGER_MS)
                animatable.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
                onColumnLanded(col)
            }
        }

        delay(3 * COLUMN_STAGGER_MS + CASCADE_FINISH_BUFFER_MS)

        onSpinFinished(isWin)
        onFinished()
    }

    Row(
        modifier = modifier
            .aspectRatio(1f)
            .clipToBounds(),
    ) {
        for (col in 0 until 3) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationY = columnOffsets[col].value
                    },
            ) {
                for (row in 0 until 3) {
                    val cell = cells.getOrNull(row)?.getOrNull(col)
                    key(spinGeneration, row, col, cell?.spinId) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
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
