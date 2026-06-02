package com.techmomentum.wc2026.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.components.AlbumSlotCard
import com.techmomentum.wc2026.ui.components.LoadingScreen
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.teamPalette
import com.techmomentum.wc2026.utils.GameConstants

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
            TopAppBar(
                title = {},
                navigationIcon = {
                    val tint = state.team?.let { teamPalette(it).primary.darken(0.3f) } ?: Color.Black
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f)),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = tint,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        if (state.loading) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }
        val team = state.team ?: return@Scaffold
        val palette = teamPalette(team)
        val total = state.slots.size.takeIf { it > 0 } ?: GameConstants.PLAYERS_PER_TEAM

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(palette.backgroundGradient)),
        ) {
            CountryAlbumBackgroundOrbs(palette)
            CountryAlbumPageFrame(
                palette = palette,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(CountryAlbumLayout.GRID_COLUMNS),
                    contentPadding = PaddingValues(CountryAlbumLayout.pagePadding),
                    horizontalArrangement = Arrangement.spacedBy(CountryAlbumLayout.gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(CountryAlbumLayout.gridSpacing),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CountryAlbumHero(
                            team = team,
                            ownedCount = state.ownedCount,
                            total = total,
                            percent = state.percent,
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SquadSummaryStrip(
                            slots = state.slots,
                            ownedCount = state.ownedCount,
                            total = total,
                            palette = palette,
                        )
                    }
                    items(state.slots, key = { it.sticker.stickerId }) { slot ->
                        AlbumSlotCard(
                            sticker = slot.sticker,
                            player = slot.player,
                            owned = slot.owned != null,
                            duplicateCount = slot.owned?.count ?: 0,
                            palette = palette,
                            onClick = { onStickerClick(slot.sticker.stickerId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryAlbumBackgroundOrbs(palette: TeamPalette) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.accentVivid.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(140f, 160f),
                        radius = 820f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.accent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(960f, 1400f),
                        radius = 720f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.primary.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(120f, 1700f),
                        radius = 620f,
                    ),
                ),
        )
    }
}

@Composable
private fun CountryAlbumPageFrame(
    palette: TeamPalette,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(CountryAlbumLayout.pageCornerRadius))
            .clip(RoundedCornerShape(CountryAlbumLayout.pageCornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFBF4),
                    ),
                ),
            ),
    ) {
        content()
    }
}
