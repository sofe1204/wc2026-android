package com.techmomentum.wc2026.ui.home



import android.app.Activity

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.relocation.BringIntoViewRequester

import androidx.compose.foundation.relocation.bringIntoViewRequester

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import com.techmomentum.wc2026.data.remote.RewardedAdManager

import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground

import com.techmomentum.wc2026.ui.album.AlbumPageFrame

import com.techmomentum.wc2026.ui.components.PixarCelebrationChip

import com.techmomentum.wc2026.ui.layout.AlbumPageScreen

import com.techmomentum.wc2026.ui.pack.AnimatedPackLoader

import com.techmomentum.wc2026.ui.pack.StickerRevealFlow

import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

import com.techmomentum.wc2026.utils.RewardEligibility

import kotlinx.coroutines.delay

import kotlinx.coroutines.launch



private const val ACTION_HIGHLIGHT_MS = 2_000L



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(

    onOpenPack: () -> Unit,

    onSettings: () -> Unit,

    onNavigateToSlots: () -> Unit,

    rewardedAdManager: RewardedAdManager? = null,

    viewModel: HomeViewModel = hiltViewModel(),

) {

    val home by viewModel.homeState.collectAsState()

    val ui by viewModel.uiState.collectAsState()

    val profile = home.profile

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val openPackRequester = remember { BringIntoViewRequester() }

    val watchAdRequester = remember { BringIntoViewRequester() }

    var highlightOpenPack by remember { mutableStateOf(false) }

    var highlightWatchAd by remember { mutableStateOf(false) }



    val displayName = profile?.firstName?.takeIf { it.isNotBlank() }

        ?: profile?.displayName?.takeIf { it.isNotBlank() }

        ?: profile?.username?.takeIf { it.isNotBlank() }?.let { "@$it" }

        ?: "Collector"

    val welcomeName = "Hey, $displayName 👋"

    val unopenedPacks = profile?.unopenedPacks ?: 0

    val slotSpinsRemaining = profile?.slotSpinsRemaining ?: 0
    val slotPacksWonToday = profile?.slotRewardPacksWonToday ?: 0
    val lastSlotSpinAd = profile?.lastRewardedSlotSpinAtEpochMs ?: 0L
    val slotSpinAdAvailable = RewardEligibility.isSlotSpinAdRewardAvailable(lastSlotSpinAd, slotPacksWonToday)
    val slotSpinAdCooldownMinutes = RewardEligibility.slotSpinAdCooldownMinutesRemaining(lastSlotSpinAd)
    val slotSpinsAvailable = RewardEligibility.isSlotSpinsRowAvailable(
        slotSpinsRemaining,
        lastSlotSpinAd,
        slotPacksWonToday,
    )



    LaunchedEffect(ui.message) {

        ui.message?.let {

            delay(4_000)

            viewModel.clearMessage()

        }

    }



    LaunchedEffect(highlightOpenPack) {

        if (highlightOpenPack) {

            delay(ACTION_HIGHLIGHT_MS)

            highlightOpenPack = false

        }

    }



    LaunchedEffect(highlightWatchAd) {

        if (highlightWatchAd) {

            delay(ACTION_HIGHLIGHT_MS)

            highlightWatchAd = false

        }

    }



    AlbumPageScreen(

        overlay = {

            if (ui.loading) {

                Box(

                    modifier = Modifier

                        .fillMaxSize()

                        .background(Color.Black.copy(alpha = 0.35f)),

                    contentAlignment = Alignment.Center,

                ) {

                    Column(

                        horizontalAlignment = Alignment.CenterHorizontally,

                        verticalArrangement = Arrangement.spacedBy(16.dp),

                    ) {

                        AnimatedPackLoader(modifier = Modifier.fillMaxWidth(0.7f))

                        Text(

                            text = "Claiming your stickers…",

                            style = MaterialTheme.typography.titleMedium,

                            fontWeight = FontWeight.Bold,

                            color = Color.White,

                        )

                    }

                }

            }



            if (ui.showStickerReveal && ui.revealed.isNotEmpty()) {

                Box(

                    modifier = Modifier

                        .fillMaxSize()

                        .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),

                ) {

                    AlbumOverviewBackground()

                    AlbumPageFrame(modifier = Modifier.fillMaxSize()) {

                        StickerRevealFlow(

                            revealed = ui.revealed,

                            revealIndex = ui.revealIndex,

                            title = "📺 Ad reward stickers!",

                            doneLabel = "Add to Album",

                            onRevealNext = viewModel::revealNextSticker,

                            onDone = viewModel::dismissStickerReveal,

                        )

                    }

                }

            }

        },

    ) {

        Column(

            modifier = Modifier

                .fillMaxSize()

                .verticalScroll(rememberScrollState())

                .padding(horizontal = 16.dp, vertical = 14.dp),

            verticalArrangement = Arrangement.spacedBy(18.dp),

        ) {

            HomeOverviewHeader(

                welcomeName = welcomeName,

                subtitle = "Your album",

                onSettings = onSettings,

            )



            HomeCollectorPanel(

                unopenedPacks = unopenedPacks,

                albumPercent = home.albumPercent,

                uniqueCount = profile?.albumUniqueCount ?: 0,

                totalCollected = profile?.totalStickerCount ?: 0,

                totalStickers = home.totalCollectibleStickers,

                loginPackAvailable = home.loginPackAvailable,

                adStickerAvailable = home.adStickerAvailable,

                adStickerCooldownMinutes = home.adStickerCooldownMinutes,

                slotSpinsRemaining = slotSpinsRemaining,
                slotPacksWonToday = slotPacksWonToday,
                slotSpinsAvailable = slotSpinsAvailable,

                slotSpinAdAvailable = slotSpinAdAvailable,

                slotSpinAdCooldownMinutes = slotSpinAdCooldownMinutes,

                loading = ui.loading,

                highlightOpenPack = highlightOpenPack,

                highlightWatchAd = highlightWatchAd,

                onOpenPack = onOpenPack,

                onWatchAd = {

                    val activity = context as? Activity

                    if (activity != null && rewardedAdManager != null) {

                        rewardedAdManager.show(

                            activity,

                            onReward = { viewModel.claimRewardedAdStickers() },

                            onDismiss = {},

                        )

                    } else {

                        viewModel.claimRewardedAdStickers()

                    }

                },

                onLoginPackTap = {

                    scope.launch {

                        openPackRequester.bringIntoView()

                        highlightOpenPack = true

                    }

                },

                onAdStickersTap = {

                    scope.launch {

                        watchAdRequester.bringIntoView()

                        highlightWatchAd = true

                    }

                },

                onSlotSpinsTap = onNavigateToSlots,

                openPackModifier = Modifier.bringIntoViewRequester(openPackRequester),

                watchAdModifier = Modifier.bringIntoViewRequester(watchAdRequester),

            )



            ui.message?.let { message ->

                PixarCelebrationChip(message = message)

            }

        }

    }

}

