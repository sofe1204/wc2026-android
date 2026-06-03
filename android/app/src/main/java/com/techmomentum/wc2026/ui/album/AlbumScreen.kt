package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.domain.usecase.TeamAlbumProgress
import com.techmomentum.wc2026.ui.components.AlbumTeamCard
import com.techmomentum.wc2026.ui.components.PixarFilterChip
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun AlbumScreen(
    onTeamClick: (String) -> Unit,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val album by viewModel.albumState.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val groups = album.groups.keys.sorted()
    val totalOwned = remember(album.groups) {
        album.groups.values.flatten().sumOf { it.ownedCount }
    }
    val visibleTeams = remember(album.groups, filter) {
        album.groups.flatMap { (_, teams) -> teams.filter { it.matchesFilter(filter) } }
    }
    val hasVisibleTeams = visibleTeams.isNotEmpty()

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
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            AlbumOverviewHeader(totalOwned = totalOwned)
                        }

                        item {
                            AlbumFilterRow(
                                groups = groups,
                                filter = filter,
                                onSelectAll = {
                                    viewModel.setGroupFilter(null)
                                    viewModel.setOwnedFilter(null)
                                },
                                onSelectGroup = viewModel::setGroupFilter,
                                onSelectOwned = { viewModel.setOwnedFilter(true) },
                                onSelectMissing = viewModel::setMissingFilter,
                            )
                        }

                        if (!hasVisibleTeams) {
                            item(key = "empty_filter") {
                                AlbumEmptyFilterState()
                            }
                        } else {
                            album.groups.forEach { (group, teams) ->
                                val filtered = teams.filter { it.matchesFilter(filter) }
                                if (filtered.isEmpty()) return@forEach

                                item(key = "header_$group") {
                                    AlbumGroupHeaderChip(group = group, teams = filtered)
                                }

                                items(filtered, key = { it.team.teamId }) { progress ->
                                    AlbumTeamCard(
                                        progress = progress,
                                        onClick = { onTeamClick(progress.team.teamId) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun TeamAlbumProgress.matchesFilter(filter: AlbumFilter): Boolean = when {
    filter.ownedOnly == true -> ownedCount > 0
    filter.missingOnly == true -> ownedCount < total
    filter.group != null -> team.group == filter.group
    else -> true
}

@Composable
private fun AlbumFilterRow(
    groups: List<String>,
    filter: AlbumFilter,
    onSelectAll: () -> Unit,
    onSelectGroup: (String) -> Unit,
    onSelectOwned: () -> Unit,
    onSelectMissing: () -> Unit,
) {
    val allSelected = filter.group == null && filter.ownedOnly != true && filter.missingOnly != true

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            PixarFilterChip(
                label = "All",
                selected = allSelected,
                onClick = onSelectAll,
            )
        }
        items(groups) { group ->
            PixarFilterChip(
                label = group.replace("Group ", ""),
                selected = filter.group == group,
                onClick = { onSelectGroup(group) },
            )
        }
        item {
            PixarFilterChip(
                label = "Owned",
                selected = filter.ownedOnly == true,
                onClick = onSelectOwned,
            )
        }
        item {
            PixarFilterChip(
                label = "Missing",
                selected = filter.missingOnly == true,
                onClick = onSelectMissing,
            )
        }
    }
}