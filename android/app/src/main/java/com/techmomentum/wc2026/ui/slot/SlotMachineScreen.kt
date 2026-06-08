package com.techmomentum.wc2026.ui.slot

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
    val slotSounds = rememberSlotSoundController()
    val activity = LocalContext.current as Activity
    val slotBitmaps = remember(viewModel, state.imageRefreshGeneration) {
        { url: String -> viewModel.slotBitmap(url) }
    }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.phase) {
        if (state.phase != SlotPhase.Spinning) {
            slotSounds.stopSpinLoop()
        }
    }

    LaunchedEffect(state.isWin, state.phase) {
        if (state.isWin && state.phase == SlotPhase.Settled && state.hapticsEnabled) {
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

            AlbumPageFrame(modifier = Modifier.fillMaxSize()) {
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
                                slotSounds = slotSounds,
                                onSpinAnimationFinished = viewModel::markSpinSettled,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        SlotCabinetPlaceholder(modifier = Modifier.fillMaxWidth())
                    }

                    if (state.isWin && state.phase == SlotPhase.Settled) {
                        PixarCelebrationChip(message = "WIN! Line match!")
                    }

                    state.message?.takeIf { state.phase == SlotPhase.Settled }?.let { message ->
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
                        onClick = {
                            slotSounds.playSpinClick()
                            viewModel.spin()
                        },
                        enabled = state.symbolsReady && !state.isAnimating && state.spinsRemaining > 0,
                        loading = state.phase == SlotPhase.Spinning,
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

private fun slotGoldFrameBrush(): Brush = Brush.linearGradient(
    listOf(
        Color(0xFFB8860B),
        CardGold,
        Color.White.copy(alpha = 0.95f),
        CardGold,
        Color(0xFFC9A227),
    ),
)

@Composable
private fun SlotMarqueeStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        AlbumPageStyle.headerAccent.darken(0.15f),
                        AlbumPageStyle.headerAccentVivid,
                        AlbumPageStyle.headerAccent.darken(0.15f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = slotGoldFrameBrush(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
            .padding(vertical = 7.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(9) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == 4) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index % 2 == 0) CardGold else Color(0xFFFFF3B0),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (index == 4) 0.7f else 0.35f),
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun SlotMachineCabinet(
    state: SlotUiState,
    slotSounds: SlotSoundController,
    onSpinAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cabinetShape = RoundedCornerShape(24.dp)
    val windowShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .collectibleShadow(
                color = CardGold.copy(alpha = 0.55f),
                elevation = 18.dp,
                shape = cabinetShape,
            )
            .clip(cabinetShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A3D52), Color(0xFF121A24)),
                ),
            )
            .border(4.dp, slotGoldFrameBrush(), cabinetShape)
            .padding(3.dp)
            .border(1.5.dp, Color.Black.copy(alpha = 0.45f), cabinetShape)
            .padding(10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SlotMarqueeStrip()
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(windowShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0A1018), Color(0xFF151F2B), Color(0xFF0A1018)),
                        ),
                    )
                    .border(2.dp, slotGoldFrameBrush(), windowShape)
                    .padding(2.dp)
                    .border(1.dp, Color.Black.copy(alpha = 0.5f), windowShape)
                    .padding(8.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.22f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        when (state.phase) {
                            SlotPhase.Spinning -> {
                                if (state.spinResultReady) {
                                    SlotCascadeBoard(
                                        cells = state.finalCells,
                                        winningCells = emptySet(),
                                        spinGeneration = state.spinGeneration,
                                        spinResultReady = state.spinResultReady,
                                        isWin = state.serverIsWin && state.localIsWin,
                                        onSpinStarted = slotSounds::startSpinLoop,
                                        onColumnLanded = slotSounds::playColumnLand,
                                        onSpinFinished = { didWin ->
                                            slotSounds.stopSpinLoop()
                                            if (didWin) {
                                                slotSounds.playWin()
                                            }
                                        },
                                        onFinished = onSpinAnimationFinished,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } else {
                                    SlotGridBoard(
                                        cells = state.finalCells,
                                        winningCells = emptySet(),
                                        spinGeneration = state.spinGeneration,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            SlotPhase.Idle, SlotPhase.Settled -> {
                                SlotGridBoard(
                                    cells = state.finalCells,
                                    winningCells = if (state.phase == SlotPhase.Settled) {
                                        state.winningCells
                                    } else {
                                        emptySet()
                                    },
                                    spinGeneration = state.spinGeneration,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
        GlossOverlay(cornerRadius = 24.dp, intensity = 0.14f)
    }
}

@Composable
private fun SlotCabinetPlaceholder(modifier: Modifier = Modifier) {
    val cabinetShape = RoundedCornerShape(24.dp)
    val windowShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cabinetShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A3D52), Color(0xFF121A24)),
                ),
            )
            .border(4.dp, slotGoldFrameBrush(), cabinetShape)
            .padding(13.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SlotMarqueeStrip()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(280.dp)
                    .clip(windowShape)
                    .background(Color(0xFF151F2B))
                    .border(2.dp, slotGoldFrameBrush(), windowShape),
            )
        }
    }
}
