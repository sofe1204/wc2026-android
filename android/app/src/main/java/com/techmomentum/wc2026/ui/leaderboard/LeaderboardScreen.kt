package com.techmomentum.wc2026.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.model.LeaderboardEntry
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.layout.AlbumPageScreen
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    AlbumPageScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent,
            )
            LeaderboardTabRow(
                selected = state.tab,
                countryName = state.result.myCountryName,
                onSelect = viewModel::selectTab,
            )
            MyRankCard(
                tab = state.tab,
                result = state.result,
            )
            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
                state.error != null -> Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                )
                else -> {
                    val rows = when (state.tab) {
                        LeaderboardTab.GLOBAL -> state.result.global
                        LeaderboardTab.COUNTRY -> state.result.country
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(rows, key = { "${it.rank}-${it.username}" }) { entry ->
                            LeaderboardRow(entry = entry)
                        }
                    }
                }
            }
            PixarSecondaryButton(
                text = "Refresh",
                onClick = viewModel::refresh,
                enabled = !state.loading,
            )
        }
    }
}

@Composable
private fun LeaderboardTabRow(
    selected: LeaderboardTab,
    countryName: String,
    onSelect: (LeaderboardTab) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PixarSecondaryButton(
            text = "Global",
            onClick = { onSelect(LeaderboardTab.GLOBAL) },
            enabled = selected != LeaderboardTab.GLOBAL,
            modifier = Modifier.weight(1f),
        )
        PixarSecondaryButton(
            text = if (countryName.isBlank()) "Country" else countryName,
            onClick = { onSelect(LeaderboardTab.COUNTRY) },
            enabled = selected != LeaderboardTab.COUNTRY,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MyRankCard(tab: LeaderboardTab, result: com.techmomentum.wc2026.data.model.LeaderboardResult) {
    val rank = when (tab) {
        LeaderboardTab.GLOBAL -> result.myGlobalRank
        LeaderboardTab.COUNTRY -> result.myCountryRank
    }
    if (result.myUsername.isBlank()) return
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(AnimePink.copy(alpha = 0.2f), CardGold.copy(alpha = 0.2f))))
            .border(1.dp, AlbumPageStyle.filterSelectedStart.copy(alpha = 0.4f), shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "@${result.myUsername}",
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.headerAccent,
        )
        Text(
            text = buildString {
                append("${result.myAlbumUniqueCount} unique")
                rank?.let { append(" · #$it") }
            },
            fontWeight = FontWeight.SemiBold,
            color = AlbumPageStyle.bottomNavUnselectedIcon,
        )
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    val shape = RoundedCornerShape(14.dp)
    val bg = if (entry.isMe) {
        Brush.horizontalGradient(listOf(AnimePink.copy(alpha = 0.25f), CardGold.copy(alpha = 0.25f)))
    } else {
        Brush.verticalGradient(AlbumPageStyle.pageFrameGradient)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#${entry.rank}",
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent,
            )
            Column {
                Text(
                    text = "@${entry.username}",
                    fontWeight = FontWeight.Bold,
                    color = AlbumPageStyle.bottomNavUnselectedLabel,
                )
                Text(
                    text = entry.countryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = AlbumPageStyle.bottomNavUnselectedIcon,
                )
            }
        }
        Text(
            text = "${entry.albumUniqueCount}",
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent,
        )
    }
}
