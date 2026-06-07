package com.techmomentum.wc2026.ui.pack

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.techmomentum.wc2026.ui.components.PixarRarityChip
import com.techmomentum.wc2026.ui.components.rarityColor
import com.techmomentum.wc2026.ui.team.GlossOverlay
import com.techmomentum.wc2026.ui.team.collectibleShadow
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.lighten

private val cardShape = RoundedCornerShape(22.dp)
private const val CARD_ASPECT = 0.7f

/** A single collectible that flips from a gold card-back to the player face. */
@Composable
fun PackRevealCard(
    item: RevealedSticker,
    faceUp: Boolean,
    modifier: Modifier = Modifier,
) {
    val flip by animateFloatAsState(
        targetValue = if (faceUp) 180f else 0f,
        animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing),
        label = "flip",
    )
    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT)
            .graphicsLayer {
                rotationY = flip
                cameraDistance = 14f * density
            },
        contentAlignment = Alignment.Center,
    ) {
        if (flip <= 90f) {
            PackCardBack(modifier = Modifier.fillMaxSize())
        } else {
            PackCardFront(
                item = item,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
            )
        }
    }
}

@Composable
private fun PackCardBack(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .collectibleShadow(color = CardGold, elevation = 16.dp, shape = cardShape)
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    listOf(CardGold, Color(0xFFFFA726), CardGold.copy(alpha = 0.85f)),
                ),
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), CardGold)),
                shape = cardShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "WC\n2026",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
        )
        GlossOverlay(
            modifier = Modifier.align(Alignment.TopCenter),
            cornerRadius = 22.dp,
            intensity = 0.4f,
        )
    }
}

@Composable
private fun PackCardFront(
    item: RevealedSticker,
    modifier: Modifier = Modifier,
) {
    val rarity = item.sticker.rarity
    val rColor = rarityColor(rarity)
    Column(
        modifier = modifier
            .collectibleShadow(color = rColor, elevation = 16.dp, shape = cardShape)
            .clip(cardShape)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(listOf(rColor, rColor.lighten(0.4f), rColor)),
                shape = cardShape,
            )
            .background(
                Brush.verticalGradient(listOf(Color.White, rColor.copy(alpha = 0.12f))),
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.78f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(rColor.copy(alpha = 0.18f), Color.White.copy(alpha = 0.9f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (!item.sticker.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.sticker.imageUrl,
                    contentDescription = item.player?.playerName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text("⚽", fontSize = 44.sp)
            }
            GlossOverlay(
                modifier = Modifier.align(Alignment.TopCenter),
                cornerRadius = 16.dp,
                intensity = 0.34f,
            )
        }
        Text(
            text = item.player?.playerName ?: item.sticker.stickerId,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A2530),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        PixarRarityChip(rarity = rarity)
    }
}

/** Wobbling, glowing booster pack shown while the pack is being opened. */
@Composable
fun AnimatedPackLoader(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "packLoader")
    val shake by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shake",
    )
    val glow by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        listOf(CardGold.copy(alpha = glow * 0.55f), Color.Transparent),
                    ),
                    shape = RoundedCornerShape(100.dp),
                ),
        )
        Box(
            modifier = Modifier
                .size(width = 132.dp, height = 172.dp)
                .graphicsLayer { rotationZ = shake }
                .collectibleShadow(color = CardGold, elevation = 18.dp, shape = cardShape)
                .clip(cardShape)
                .background(
                    Brush.linearGradient(
                        listOf(CardGold, Color(0xFFFFA726), CardGold.copy(alpha = 0.9f)),
                    ),
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), CardGold)),
                    shape = cardShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("🎁", fontSize = 56.sp)
            GlossOverlay(
                modifier = Modifier.align(Alignment.TopCenter),
                cornerRadius = 22.dp,
                intensity = 0.42f,
            )
        }
    }
}

/** A horizontal strip of the already-revealed stickers, kept small below the hero card. */
@Composable
fun RevealedStrip(
    items: List<RevealedSticker>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        items(items, key = { it.sticker.stickerId }) { item ->
            val rColor = rarityColor(item.sticker.rarity)
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, rColor, RoundedCornerShape(8.dp))
                    .background(rColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                if (!item.sticker.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.sticker.imageUrl,
                        contentDescription = item.player?.playerName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text("⚽", fontSize = 18.sp)
                }
            }
        }
    }
}

/** Soft rarity-colored glow placed behind the hero card. */
@Composable
fun RarityGlow(
    color: Color,
    intense: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "rarityGlow")
    val pulse by transition.animateFloat(
        initialValue = if (intense) 0.45f else 0.25f,
        targetValue = if (intense) 0.85f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.radialGradient(
                    listOf(color.copy(alpha = pulse), Color.Transparent),
                ),
            ),
    )
}
