package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.ui.team.CountryAlbumLayout
import com.techmomentum.wc2026.ui.team.GlossOverlay
import com.techmomentum.wc2026.ui.team.collectibleShadow
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.lighten
import com.techmomentum.wc2026.ui.theme.positionAbbrev

@Composable
fun AlbumSlotCard(
    sticker: Sticker,
    player: Player?,
    owned: Boolean,
    duplicateCount: Int,
    palette: TeamPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rarity = sticker.rarity
    val rarity0 = rarityColor(rarity)
    val artCorner = 16.dp
    val shape = RoundedCornerShape(CountryAlbumLayout.slotCornerRadius)
    val shadowColor = if (owned) rarity0 else palette.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .collectibleShadow(
                color = shadowColor,
                elevation = if (owned) 12.dp else 9.dp,
                shape = shape,
            )
            .clip(shape)
            .border(
                width = if (owned) 2.5.dp else 2.dp,
                brush = if (owned) {
                    Brush.linearGradient(listOf(rarity0, rarity0.lighten(0.4f), rarity0))
                } else {
                    Brush.linearGradient(
                        listOf(
                            palette.primary.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.7f),
                            palette.primary.copy(alpha = 0.35f),
                        ),
                    )
                },
                shape = shape,
            )
            .background(Brush.verticalGradient(palette.cardGradient))
            .clickable(onClick = onClick)
            .padding(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CountryAlbumLayout.SLOT_ASPECT_RATIO)
                .clip(RoundedCornerShape(artCorner))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.primary.copy(alpha = if (owned) 0.10f else 0.20f),
                            palette.accentVivid.copy(alpha = if (owned) 0.06f else 0.14f),
                            Color.White.copy(alpha = 0.9f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                owned && !sticker.imageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = sticker.imageUrl,
                        contentDescription = player?.playerName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                else -> {
                    JerseyArt(
                        shirtNumber = player?.shirtNumber,
                        teamColor = palette.primary,
                        accent = palette.accentVivid,
                        owned = owned,
                    )
                }
            }

            GlossOverlay(
                modifier = Modifier.align(Alignment.TopCenter),
                cornerRadius = artCorner,
                intensity = if (owned) 0.32f else 0.22f,
            )

            // Sticker number badge.
            Text(
                text = "#${sticker.stickerNumber}",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(7.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(palette.primary, palette.primary.darken(0.18f)),
                        ),
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            if (!owned) {
                MissingLockPill(
                    palette = palette,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp),
                )
            }

            if (owned && duplicateCount > 1) {
                Text(
                    text = "×$duplicateCount",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(7.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brush.horizontalGradient(listOf(AnimePink, CardGold)))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Info strip.
        Text(
            text = player?.playerName ?: sticker.stickerId,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = palette.primary.darken(0.45f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            player?.let {
                Text(
                    text = positionAbbrev(it.position),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.primary.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary.darken(0.3f),
                )
            }
            PixarRarityChip(rarity = rarity, muted = !owned)
        }
    }
}
