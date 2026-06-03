package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.ui.team.CountryAlbumLayout
import com.techmomentum.wc2026.ui.team.GlossOverlay
import com.techmomentum.wc2026.ui.team.collectibleShadow
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.lighten

/**
 * Dedicated team crest collectible — shown above the player grid (Option A).
 */
@Composable
fun TeamCrestSlotCard(
    team: Team,
    sticker: Sticker,
    owned: Boolean,
    duplicateCount: Int,
    palette: TeamPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rarity = sticker.rarity
    val rarity0 = rarityColor(rarity)
    val shape = RoundedCornerShape(CountryAlbumLayout.slotCornerRadius)
    val artCorner = 16.dp
    val shadowColor = if (owned) rarity0 else palette.primary

    Column(
        modifier = modifier
            .fillMaxWidth(CountryAlbumLayout.crestSlotWidthFraction)
            .collectibleShadow(
                color = shadowColor,
                elevation = if (owned) 13.dp else 10.dp,
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
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CountryAlbumLayout.CREST_SLOT_ASPECT_RATIO)
                .clip(RoundedCornerShape(artCorner))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.primary.copy(alpha = if (owned) 0.12f else 0.22f),
                            palette.accentVivid.copy(alpha = if (owned) 0.08f else 0.16f),
                            Color.White.copy(alpha = 0.92f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                owned && sticker.imageUrl.isNotBlank() -> {
                    AsyncImage(
                        model = sticker.imageUrl,
                        contentDescription = "${team.countryName} team emblem",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
                owned && team.customEmblemUrl.isNotBlank() -> {
                    AsyncImage(
                        model = team.customEmblemUrl,
                        contentDescription = "${team.countryName} team emblem",
                        modifier = Modifier.fillMaxSize(0.88f),
                        contentScale = ContentScale.Fit,
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.82f)
                            .aspectRatio(0.85f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CrestSilhouette(
                            fill = Brush.verticalGradient(
                                if (owned) {
                                    listOf(
                                        palette.primary.lighten(0.1f),
                                        palette.primary,
                                        palette.accentVivid,
                                    )
                                } else {
                                    listOf(
                                        palette.primary.copy(alpha = 0.42f),
                                        palette.primary.copy(alpha = 0.30f),
                                    )
                                },
                            ),
                            modifier = Modifier.fillMaxSize(),
                            outline = Color.White.copy(alpha = if (owned) 0.8f else 0.55f),
                        )
                    }
                }
            }

            GlossOverlay(
                modifier = Modifier.align(Alignment.TopCenter),
                cornerRadius = artCorner,
                intensity = if (owned) 0.34f else 0.24f,
            )

            Text(
                text = "Crest",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(palette.primary, palette.primary.darken(0.18f)),
                        ),
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            if (!owned) {
                MissingLockPill(
                    palette = palette,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }

            if (owned && duplicateCount > 1) {
                Text(
                    text = "×$duplicateCount",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brush.horizontalGradient(listOf(AnimePink, CardGold)))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Team emblem",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = palette.primary.darken(0.45f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = team.flagEmoji.ifBlank { team.teamCode },
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(palette.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
            )
            PixarRarityChip(rarity = rarity, muted = !owned)
        }
    }
}
