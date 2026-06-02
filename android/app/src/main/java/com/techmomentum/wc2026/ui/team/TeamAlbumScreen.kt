package com.techmomentum.wc2026.ui.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.components.LoadingScreen
import com.techmomentum.wc2026.ui.components.StickerCard
import com.techmomentum.wc2026.ui.components.TeamEmblem
import com.techmomentum.wc2026.ui.components.WorldCupTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAlbumScreen(
    onStickerClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TeamAlbumViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            WorldCupTopBar(
                title = state.team?.countryName ?: "Team",
                subtitle = state.team?.group,
                showBack = true,
                onBack = onBack,
            )
        },
    ) { padding ->
        if (state.loading) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
        ) {
            state.team?.let { team ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                ) {
                    TeamEmblem(team = team, size = 72.dp)
                    Text(team.countryName, style = MaterialTheme.typography.headlineSmall)
                    Text("${state.slots.count { it.owned != null }} / ${state.slots.size} stickers", style = MaterialTheme.typography.bodyMedium)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.slots, key = { it.sticker.stickerId }) { slot ->
                    StickerCard(
                        sticker = slot.sticker,
                        player = slot.player,
                        owned = slot.owned != null,
                        duplicateCount = slot.owned?.count ?: 0,
                        onClick = { onStickerClick(slot.sticker.stickerId) },
                    )
                }
            }
        }
    }
}
