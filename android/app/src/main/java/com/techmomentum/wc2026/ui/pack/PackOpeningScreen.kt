package com.techmomentum.wc2026.ui.pack

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.components.ErrorState
import com.techmomentum.wc2026.ui.components.RarityBadge
import com.techmomentum.wc2026.ui.components.StickerCard
import com.techmomentum.wc2026.ui.components.WorldCupTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackOpeningScreen(
    onDone: () -> Unit,
    viewModel: PackOpeningViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (state.revealed.isEmpty() && !state.loading && state.error == null) {
            viewModel.openPack()
        }
    }

    Scaffold(
        topBar = { WorldCupTopBar(title = "Pack Opening", showBack = true, onBack = onDone) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("✨ Reveal your stickers!", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            when {
                state.loading -> CircularProgressIndicator()
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::openPack)
                else -> {
                    val visible = state.revealed.take(state.revealIndex + 1)
                    visible.forEachIndexed { index, item ->
                        val alpha by animateFloatAsState(if (index <= state.revealIndex) 1f else 0f, label = "reveal")
                        Column(
                            modifier = Modifier.fillMaxWidth().alpha(alpha),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            StickerCard(
                                sticker = item.sticker,
                                player = item.player,
                                owned = true,
                                duplicateCount = 1,
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(0.65f),
                            )
                            RarityBadge(rarity = item.sticker.rarity)
                        }
                    }
                    if (!state.finished && state.revealIndex < state.revealed.lastIndex) {
                        Button(onClick = viewModel::revealNext, modifier = Modifier.fillMaxWidth()) {
                            Text("Reveal Next")
                        }
                    } else if (state.finished || (state.revealed.isNotEmpty() && state.revealIndex >= state.revealed.lastIndex)) {
                        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Add to Album") }
                    }
                }
            }
        }
    }
}
