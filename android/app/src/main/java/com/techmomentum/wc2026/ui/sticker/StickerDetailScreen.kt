package com.techmomentum.wc2026.ui.sticker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.techmomentum.wc2026.ui.components.LoadingScreen
import com.techmomentum.wc2026.ui.components.RarityBadge
import com.techmomentum.wc2026.ui.components.TeamEmblem
import com.techmomentum.wc2026.ui.components.WorldCupTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerDetailScreen(
    onBack: () -> Unit,
    viewModel: StickerDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            WorldCupTopBar(
                title = state.player?.playerName ?: "Sticker",
                subtitle = state.team?.countryName,
                showBack = true,
                onBack = onBack,
            )
        },
    ) { padding ->
        if (state.loading) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }
        val sticker = state.sticker
        val player = state.player
        val owned = state.owned

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.team?.let { TeamEmblem(team = it, size = 56.dp) }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (!sticker?.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = sticker?.imageUrl,
                            contentDescription = player?.playerName,
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Text("⚽", style = MaterialTheme.typography.displayLarge)
                    }
                }
            }
            player?.let { RarityBadge(rarity = it.rarity) }
            Text(player?.playerName ?: "", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text("${state.team?.countryName} • ${sticker?.group}", style = MaterialTheme.typography.bodyLarge)
            player?.let { Text("#${it.shirtNumber} • ${it.position}") }
            Text(
                if (owned != null) "Collected (×${owned.count})" else "Missing",
                style = MaterialTheme.typography.titleMedium,
                color = if (owned != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )
        }
    }
}
