package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.data.model.isTeamEmblem
import com.techmomentum.wc2026.ui.team.CountryAlbumLayout
import com.techmomentum.wc2026.ui.team.GlossOverlay
import com.techmomentum.wc2026.ui.team.collectibleShadow
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.lighten
import com.techmomentum.wc2026.ui.theme.positionAbbrev

private val detailCornerRadius = 26.dp
private val detailArtCorner = 18.dp

/**
 * Large collectible frame — same visual language as [AlbumSlotCard], without the name row.
 */
@Composable
fun StickerDetailHeroCard(
    team: Team,
    sticker: Sticker,
    player: Player?,
    owned: Boolean,
    duplicateCount: Int,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    val isEmblem = sticker.isTeamEmblem()
    val rarity = sticker.rarity
    val rarity0 = rarityColor(rarity)
    val shape = RoundedCornerShape(detailCornerRadius)
    val shadowColor = if (owned) rarity0 else palette.primary

    Column(
        modifier = modifier
            .collectibleShadow(
                color = shadowColor,
                elevation = if (owned) 14.dp else 11.dp,
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
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CountryAlbumLayout.SLOT_ASPECT_RATIO)
                .clip(RoundedCornerShape(detailArtCorner))
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
                isEmblem && owned && !sticker.imageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = sticker.imageUrl,
                        contentDescription = "${team.countryName} team emblem",
                        modifier = Modifier.fillMaxSize(0.92f),
                        contentScale = ContentScale.Fit,
                    )
                }
                isEmblem && owned && team.customEmblemUrl.isNotBlank() -> {
                    AsyncImage(
                        model = team.customEmblemUrl,
                        contentDescription = "${team.countryName} team emblem",
                        modifier = Modifier.fillMaxSize(0.92f),
                        contentScale = ContentScale.Fit,
                    )
                }
                isEmblem -> {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.78f)
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
                cornerRadius = detailArtCorner,
                intensity = if (owned) 0.34f else 0.24f,
            )

            if (!owned) {
                MissingLockPill(
                    palette = palette,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                )
            }

            if (owned && duplicateCount > 1) {
                Text(
                    text = "×$duplicateCount",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brush.horizontalGradient(listOf(AnimePink, CardGold)))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        StickerDetailChipRow(
            team = team,
            player = player,
            isTeamEmblem = isEmblem,
            rarity = rarity,
            owned = owned,
            duplicateCount = duplicateCount,
            palette = palette,
        )
    }
}

@Composable
fun StickerDetailChipRow(
    team: Team,
    player: Player?,
    isTeamEmblem: Boolean,
    rarity: Rarity,
    owned: Boolean,
    duplicateCount: Int,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            isTeamEmblem -> {
                Text(
                    text = team.flagEmoji.ifBlank { team.teamCode },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.primary.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "Crest",
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.primary.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary.darken(0.3f),
                )
            }
            player != null -> {
                Text(
                    text = positionAbbrev(player.position),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.primary.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary.darken(0.3f),
                )
            }
        }
        PixarRarityChip(rarity = rarity, muted = !owned)
        if (owned) {
            CollectedCelebrationChip(count = duplicateCount)
        }
    }
}

@Composable
fun MissingLockPill(
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(palette.primary.darken(0.1f), palette.primary),
                ),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = "Missing",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun CollectedCelebrationChip(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(AnimePink, CardGold)))
            .padding(horizontal = 11.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "✨",
            fontSize = 11.sp,
        )
        Text(
            text = if (count > 1) "Collected ×$count" else "Collected",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}
