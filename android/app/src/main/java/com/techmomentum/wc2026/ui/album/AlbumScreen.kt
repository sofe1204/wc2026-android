package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import com.techmomentum.wc2026.domain.usecase.TeamAlbumProgress
import com.techmomentum.wc2026.ui.components.AlbumProgressBar
import com.techmomentum.wc2026.ui.components.TeamEmblem
import com.techmomentum.wc2026.ui.components.WorldCupTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    onTeamClick: (String) -> Unit,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val album by viewModel.albumState.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val groups = album.groups.keys.sorted()

    Scaffold(
        topBar = { WorldCupTopBar(title = "My Album", subtitle = "Groups A – L") },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = filter.group == null && filter.ownedOnly != true && filter.missingOnly != true,
                        onClick = {
                            viewModel.setGroupFilter(null)
                            viewModel.setOwnedFilter(null)
                        },
                        label = { Text("All") },
                    )
                }
                items(groups) { group ->
                    FilterChip(
                        selected = filter.group == group,
                        onClick = { viewModel.setGroupFilter(group) },
                        label = { Text(group.replace("Group ", "")) },
                    )
                }
                item {
                    FilterChip(selected = filter.ownedOnly == true, onClick = { viewModel.setOwnedFilter(true) }, label = { Text("Owned") })
                }
                item {
                    FilterChip(selected = filter.missingOnly == true, onClick = { viewModel.setMissingFilter() }, label = { Text("Missing") })
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                album.groups.forEach { (group, teams) ->
                    val filtered = teams.filter { team ->
                        when {
                            filter.ownedOnly == true -> team.ownedCount > 0
                            filter.missingOnly == true -> team.ownedCount < team.total
                            filter.group != null -> team.team.group == filter.group
                            else -> true
                        }
                    }
                    if (filtered.isEmpty()) return@forEach
                    item { Text(group, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(filtered, key = { it.team.teamId }) { progress ->
                        TeamRow(progress, onClick = { onTeamClick(progress.team.teamId) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamRow(progress: TeamAlbumProgress, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TeamEmblem(team = progress.team, size = 44.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text("${progress.team.flagEmoji} ${progress.team.countryName}", style = MaterialTheme.typography.titleMedium)
            Text(progress.team.group, style = MaterialTheme.typography.bodySmall)
            AlbumProgressBar(
                label = "Stickers",
                progress = progress.percent,
                detail = "${progress.ownedCount} / ${progress.total}",
            )
        }
    }
}
