package com.techmomentum.wc2026.ui.pack

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.remote.InterstitialAdManager
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.ErrorState
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.WorldCupTopBar
import com.techmomentum.wc2026.ui.components.rarityColor
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.CardGold
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackOpeningScreen(
    interstitialAdManager: InterstitialAdManager,
    onDone: () -> Unit,
    viewModel: PackOpeningViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        interstitialAdManager.load()
        if (state.revealed.isEmpty() && !state.loading && state.error == null) {
            viewModel.openPack()
        }
    }

    val finishPackOpening: () -> Unit = {
        if (activity != null) {
            interstitialAdManager.show(activity, onDismiss = onDone)
        } else {
            onDone()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { WorldCupTopBar(title = "Pack Opening", showBack = true, onBack = finishPackOpening) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),
        ) {
            AlbumOverviewBackground()

            AlbumPageFrame(modifier = Modifier.fillMaxSize()) {
                when {
                    state.loading -> LoadingContent()
                    state.error != null -> Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ErrorState(state.error!!, onRetry = viewModel::openPack)
                    }
                    state.revealed.isNotEmpty() -> RevealContent(
                        state = state,
                        onRevealNext = viewModel::revealNext,
                        onDone = finishPackOpening,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedPackLoader(modifier = Modifier.fillMaxWidth())
        Text(
            text = "Opening your pack…",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.headerAccent,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}

@Composable
private fun RevealContent(
    state: PackOpeningUiState,
    onRevealNext: () -> Unit,
    onDone: () -> Unit,
) {
    val revealIndex = state.revealIndex.coerceIn(0, state.revealed.lastIndex)
    val current = state.revealed[revealIndex]
    val rarity = current.sticker.rarity
    val isRarePull = rarity.ordinal >= Rarity.RARE.ordinal
    val isLast = revealIndex >= state.revealed.lastIndex

    // Flip + bouncy entrance, re-triggered each time we move to a new card.
    var faceUp by remember(revealIndex) { mutableStateOf(false) }
    val entrance = remember(revealIndex) { Animatable(0.86f) }
    LaunchedEffect(revealIndex) {
        entrance.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(revealIndex) {
        delay(150)
        faceUp = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "✨ Reveal your stickers!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "${revealIndex + 1} / ${state.revealed.size}",
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AlbumPageStyle.headerAccent.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.headerAccent,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            RarityGlow(
                color = if (isRarePull) rarityColor(rarity) else CardGold,
                intense = isRarePull,
            )
            PackRevealCard(
                item = current,
                faceUp = faceUp,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .graphicsLayer {
                        scaleX = entrance.value
                        scaleY = entrance.value
                    },
            )
        }

        if (faceUp && isRarePull) {
            val label = rarity.name.lowercase().replaceFirstChar { it.uppercase() }
            PixarCelebrationChip(message = "$label pull!")
        }

        if (revealIndex > 0) {
            RevealedStrip(items = state.revealed.take(revealIndex))
        }

        if (isLast) {
            PixarPrimaryButton(text = "Add to Album", onClick = onDone)
        } else {
            PixarPrimaryButton(text = "Reveal Next", onClick = onRevealNext)
        }
    }
}
