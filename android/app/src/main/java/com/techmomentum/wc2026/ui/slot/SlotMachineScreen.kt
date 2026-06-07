package com.techmomentum.wc2026.ui.slot

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.ui.components.WorldCupTopBar
import com.techmomentum.wc2026.ui.theme.CardGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotMachineScreen(
    rewardedAdManager: RewardedAdManager,
    viewModel: SlotViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity

    Scaffold(
        topBar = { WorldCupTopBar(title = "Slot Machine", subtitle = "Match 3 symbols to win packs") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Spins left: ${state.spinsRemaining}")
                    Text("Packs won today: ${state.packsWonToday}")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, CardGold, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.grid.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { symbol ->
                            SlotReelCell(symbol = symbol, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            if (state.isWin) {
                Text("🎉 WIN!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            state.message?.let { Text(it, textAlign = TextAlign.Center) }
            Button(
                onClick = viewModel::spin,
                enabled = !state.loading && state.spinsRemaining > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.loading) CircularProgressIndicator() else Text("Spin")
            }
            OutlinedButton(
                onClick = { viewModel.watchAdForSpins(activity, rewardedAdManager) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Watch Ad (+5 Spins)") }
        }
    }
}

@Composable
private fun SlotReelCell(symbol: SlotSymbol?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        when {
            symbol != null && symbol.imageUrl.isNotBlank() -> {
                AsyncImage(
                    model = symbol.imageUrl,
                    contentDescription = symbol.label,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            symbol != null -> {
                Text(
                    text = symbol.label.take(10),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                )
            }
            else -> {
                Text(
                    text = "?",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
