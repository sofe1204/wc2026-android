package com.techmomentum.wc2026.ui.sticker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.model.hasCompleteRatings
import com.techmomentum.wc2026.data.model.isTeamEmblem
import com.techmomentum.wc2026.ui.components.LoadingScreen
import com.techmomentum.wc2026.ui.components.StickerDetailHeroCard
import com.techmomentum.wc2026.ui.team.CountryAlbumBackgroundOrbs
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.teamPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerDetailScreen(
    onBack: () -> Unit,
    viewModel: StickerDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            val tint = state.team?.let { teamPalette(it).primary.darken(0.3f) } ?: Color.Black
            TopAppBar(
                title = {},
                navigationIcon = {
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
        val sticker = state.sticker ?: return@Scaffold
        val team = state.team ?: return@Scaffold
        val player = state.player
        val owned = state.owned
        val isOwned = owned != null
        val palette = teamPalette(team)
        val isEmblem = sticker.isTeamEmblem()
        val showStats = !isEmblem && player != null

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(palette.backgroundGradient)),
        ) {
            CountryAlbumBackgroundOrbs(palette)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StickerDetailHeader(
                    team = team,
                    player = player,
                    sticker = sticker,
                    palette = palette,
                )

                Box(
                    modifier = Modifier.fillMaxWidth(0.82f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        palette.primary.copy(alpha = 0.22f),
                                        palette.accentVivid.copy(alpha = 0.10f),
                                        Color.Transparent,
                                    ),
                                    radius = 480f,
                                ),
                            ),
                    )
                    StickerDetailHeroCard(
                        team = team,
                        sticker = sticker,
                        player = player,
                        owned = isOwned,
                        duplicateCount = owned?.count ?: 0,
                        palette = palette,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (showStats) {
                    val p = player!!
                    if (p.hasCompleteRatings()) {
                        if (p.clubName.isNotBlank() || p.clubLeague.isNotBlank()) {
                            StickerDetailClubCard(
                                player = p,
                                palette = palette,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        StickerDetailRatingsCard(
                            player = p,
                            palette = palette,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        StickerDetailStatsUnavailable(
                            palette = palette,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
