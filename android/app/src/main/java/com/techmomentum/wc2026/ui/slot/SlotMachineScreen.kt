package com.techmomentum.wc2026.ui.slot

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.components.PixarStatusChip
import com.techmomentum.wc2026.ui.team.GlossOverlay
import com.techmomentum.wc2026.ui.team.collectibleShadow
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.utils.GameConstants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SlotMachineScreen(
    rewardedAdManager: RewardedAdManager,
    viewModel: SlotViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity
    val slotBitmaps = remember(viewModel) { { url: String -> viewModel.slotBitmap(url) } }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.isWin, state.spinPhase) {
        if (state.isWin && state.spinPhase == SlotSpinPhase.Idle && state.hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),
        ) {
            AlbumOverviewBackground()

            AlbumPageFrame(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Slot Machine",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = AlbumPageStyle.headerAccent,
                        )
                        Text(
                            text = "Match 3 in a row (horizontal or diagonal) to win a pack · " +
                                "${GameConstants.DAILY_FREE_SLOT_SPINS} free spins/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlbumPageStyle.bottomNavUnselectedIcon,
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PixarStatusChip(
                            label = "Spins left · ${state.spinsRemaining}",
                            available = state.spinsRemaining > 0,
                        )
                        PixarStatusChip(
                            label = "Packs won · ${state.packsWonToday}/${GameConstants.DAILY_SLOT_PACK_REWARD_CAP}",
                            available = state.packsWonToday < GameConstants.DAILY_SLOT_PACK_REWARD_CAP,
                        )
                    }

                    if (state.symbolsReady) {
                        CompositionLocalProvider(LocalSlotBitmaps provides slotBitmaps) {
                            SlotMachineCabinet(
                                state = state,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        SlotCabinetPlaceholder(modifier = Modifier.fillMaxWidth())
                    }

                    if (state.isWin && state.spinPhase == SlotSpinPhase.Idle) {
                        PixarCelebrationChip(message = "WIN! Line match!")
                    }

                    state.message?.takeIf { state.spinPhase == SlotSpinPhase.Idle }?.let { message ->
                        Text(
                            text = message,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AlbumPageStyle.headerAccent.darken(0.1f),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    PixarPrimaryButton(
                        text = if (state.isAnimating) "Spinning…" else "Spin",
                        onClick = viewModel::spin,
                        enabled = state.symbolsReady && !state.isAnimating && state.spinsRemaining > 0,
                        loading = state.spinPhase == SlotSpinPhase.Spinning,
                    )

                    PixarSecondaryButton(
                        text = if (state.slotSpinAdAvailable) {
                            "Watch Ad (+${GameConstants.REWARDED_SLOT_SPINS} Spins)"
                        } else {
                            "Spin ad · ${state.slotSpinAdCooldownMinutes}m"
                        },
                        onClick = { viewModel.watchAdForSpins(activity, rewardedAdManager) },
                        enabled = state.slotSpinAdAvailable && !state.isAnimating,
                        accentBorder = state.slotSpinAdAvailable,
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotMachineCabinet(
    state: SlotUiState,
    modifier: Modifier = Modifier,
) {
    val cabinetShape = RoundedCornerShape(22.dp)
    val goldFrame = Brush.linearGradient(
        listOf(
            CardGold,
            Color.White.copy(alpha = 0.9f),
            CardGold.copy(alpha = 0.85f),
        ),
    )

    Box(
        modifier = modifier
            .collectibleShadow(
                color = CardGold.copy(alpha = 0.65f),
                elevation = 14.dp,
                shape = cabinetShape,
            )
            .clip(cabinetShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF243447), Color(0xFF141C28)),
                ),
            )
            .border(3.dp, goldFrame, cabinetShape)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E2D3D), Color(0xFF121A24)),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    color = CardGold.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val sourceGrid = when (state.spinPhase) {
                SlotSpinPhase.Settling -> state.targetGrid
                else -> state.grid
            }

            repeat(3) { columnIndex ->
                key(columnIndex) {
                val isColumnSpinning = SlotViewModel.isColumnSpinning(
                    phase = state.spinPhase,
                    settledColumnCount = state.settledColumnCount,
                    column = columnIndex,
                )
                val stopRequested = SlotViewModel.isColumnStopRequested(
                    phase = state.spinPhase,
                    settledColumnCount = state.settledColumnCount,
                    column = columnIndex,
                )
                val columnSymbols = SlotViewModel.columnSymbols(sourceGrid, columnIndex)
                val winningRows = if (state.spinPhase == SlotSpinPhase.Idle && state.isWin) {
                    state.winningCells.filter { it.second == columnIndex }.map { it.first }.toSet()
                } else {
                    emptySet()
                }

                SlotReelColumn(
                    columnIndex = columnIndex,
                    finalSymbols = columnSymbols,
                    isColumnSpinning = isColumnSpinning,
                    stopRequested = stopRequested,
                    symbolPool = state.symbolPool,
                    spinGeneration = state.spinGeneration,
                    winningRows = winningRows,
                    modifier = Modifier.weight(1f),
                )
                }
            }
        }
        GlossOverlay(cornerRadius = 22.dp, intensity = 0.12f)
    }
}

@Composable
private fun SlotCabinetPlaceholder(modifier: Modifier = Modifier) {
    val cabinetShape = RoundedCornerShape(22.dp)
    val goldFrame = Brush.linearGradient(
        listOf(
            CardGold,
            Color.White.copy(alpha = 0.9f),
            CardGold.copy(alpha = 0.85f),
        ),
    )

    Box(
        modifier = modifier
            .aspectRatio(1.15f)
            .clip(cabinetShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF243447), Color(0xFF141C28)),
                ),
            )
            .border(3.dp, goldFrame, cabinetShape)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E2D3D), Color(0xFF121A24)),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    color = CardGold.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A3848).copy(alpha = 0.55f)),
                        )
                    }
                }
            }
        }
    }
}
