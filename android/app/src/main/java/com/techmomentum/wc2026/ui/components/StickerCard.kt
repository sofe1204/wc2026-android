package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Sticker

@Composable
fun StickerCard(
    sticker: Sticker,
    player: Player?,
    owned: Boolean,
    duplicateCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rarity = sticker.rarity
    val borderColor = rarityColor(rarity)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (owned) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    listOf(borderColor, borderColor.copy(alpha = 0.4f)),
                ),
                shape = RoundedCornerShape(12.dp),
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (owned) 1f else 0.5f))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            if (owned && !sticker.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = sticker.imageUrl,
                    contentDescription = player?.playerName,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.75f),
                    contentScale = ContentScale.Crop,
                )
            } else if (owned) {
                Text("⚽", style = MaterialTheme.typography.headlineLarge)
            } else {
                Text("?", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.outline)
            }
        }
        Text(
            player?.playerName ?: sticker.stickerId,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        if (owned && duplicateCount > 1) {
            Text("×$duplicateCount", style = MaterialTheme.typography.labelSmall)
        }
        RarityBadge(rarity = rarity)
    }
}
