package com.techmomentum.wc2026.ui.home

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.ui.components.AlbumProgressBar
import com.techmomentum.wc2026.ui.components.PackCard
import com.techmomentum.wc2026.ui.components.WorldCupTopBar
import com.techmomentum.wc2026.utils.DateUtils
import com.techmomentum.wc2026.utils.GameConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenPack: () -> Unit,
    onSettings: () -> Unit,
    isGuest: Boolean,
    rewardedAdManager: RewardedAdManager? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val home by viewModel.homeState.collectAsState()
    val ui by viewModel.uiState.collectAsState()
    val profile = home.profile
    val context = LocalContext.current

    Scaffold(
        topBar = {
            WorldCupTopBar(
                title = "World Cup 2026",
                subtitle = if (isGuest) "Guest • Offline demo" else "Sticker Album",
                showSettings = true,
                onSettings = onSettings,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Welcome, ${profile?.displayName?.ifBlank { profile.email } ?: "Collector"}",
                style = MaterialTheme.typography.titleLarge,
            )
            PackCard(count = profile?.unopenedPacks ?: 0, modifier = Modifier.fillMaxWidth())
            AlbumProgressBar(
                label = "Album progress",
                progress = home.albumPercent,
                detail = "${profile?.albumUniqueCount ?: 0} / ${GameConstants.TOTAL_STICKERS} unique",
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Daily rewards", style = MaterialTheme.typography.titleSmall)
                    Text("Daily packs: ${if (home.dailyClaimedToday) "✓ Claimed" else "○ Available"}")
                    Text("Ad bonus pack: ${if (home.adPackClaimedToday) "✓ Claimed" else "○ Available"}")
                    Text("Slot spins left: ${profile?.slotSpinsRemaining ?: 0}")
                    Text("Slot packs won today: ${profile?.slotRewardPacksWonToday ?: 0}/${GameConstants.DAILY_SLOT_PACK_REWARD_CAP}")
                }
            }
            ui.message?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = onOpenPack,
                enabled = (profile?.unopenedPacks ?: 0) > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Open Sticker Pack") }
            OutlinedButton(
                onClick = viewModel::claimDailyPacks,
                enabled = !ui.loading && !home.dailyClaimedToday,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Claim Daily Packs (+${GameConstants.DAILY_FREE_PACKS})") }
            OutlinedButton(
                onClick = {
                    val activity = context as? Activity
                    if (activity != null && rewardedAdManager != null) {
                        rewardedAdManager.show(activity, onReward = { viewModel.claimRewardedAdPack() }, onDismiss = {})
                    } else {
                        viewModel.claimRewardedAdPack()
                    }
                },
                enabled = !ui.loading && !home.adPackClaimedToday,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Watch Ad for Bonus Pack") }
        }
    }
}
