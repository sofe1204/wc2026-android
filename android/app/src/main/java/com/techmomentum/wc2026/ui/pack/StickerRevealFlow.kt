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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.rarityColor
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.CardGold
import kotlinx.coroutines.delay

@Composable
fun StickerRevealFlow(
    revealed: List<RevealedSticker>,
    revealIndex: Int,
    title: String,
    doneLabel: String,
    onRevealNext: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (revealed.isEmpty() || revealIndex < 0) return

    val safeIndex = revealIndex.coerceIn(0, revealed.lastIndex)
    val current = revealed[safeIndex]
    val rarity = current.sticker.rarity
    val isRarePull = rarity.ordinal >= Rarity.RARE.ordinal
    val isLast = safeIndex >= revealed.lastIndex

    var faceUp by remember(safeIndex) { mutableStateOf(false) }
    val entrance = remember(safeIndex) { Animatable(0.86f) }
    LaunchedEffect(safeIndex) {
        entrance.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
    LaunchedEffect(safeIndex) {
        delay(150)
        faceUp = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "${safeIndex + 1} / ${revealed.size}",
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

        if (safeIndex > 0) {
            RevealedStrip(items = revealed.take(safeIndex))
        }

        if (isLast) {
            PixarPrimaryButton(text = doneLabel, onClick = onDone)
        } else {
            PixarPrimaryButton(text = "Reveal Next", onClick = onRevealNext)
        }
    }
}
