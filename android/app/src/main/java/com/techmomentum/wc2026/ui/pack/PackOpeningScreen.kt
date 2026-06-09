package com.techmomentum.wc2026.ui.pack

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
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.data.remote.InterstitialAdManager
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.ErrorState
import com.techmomentum.wc2026.ui.components.WorldCupTopBar
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

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
                    state.revealed.isNotEmpty() -> StickerRevealFlow(
                        revealed = state.revealed,
                        revealIndex = state.revealIndex,
                        title = "✨ Reveal your stickers!",
                        doneLabel = "Add to Album",
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

