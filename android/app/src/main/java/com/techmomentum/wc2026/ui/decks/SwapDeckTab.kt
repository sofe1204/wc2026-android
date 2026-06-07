package com.techmomentum.wc2026.ui.decks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.techmomentum.wc2026.data.model.SwapDeckItem
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.utils.GameConstants

@Composable
fun SwapDeckTab(
    viewModel: SwapDeckViewModel = hiltViewModel(),
) {
    val deck by viewModel.swapDeckState.collectAsState()
    val ui by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Extras from your album stay here. Trade ${GameConstants.SWAP_DUPLICATES_FOR_PACK} " +
                "duplicates for 1 new pack.",
            style = MaterialTheme.typography.bodyMedium,
            color = AlbumPageStyle.bottomNavUnselectedIcon,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        Text(
            text = "Swap deck · ${deck.totalDuplicates} duplicates",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.headerAccent,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        ui.message?.let { PixarCelebrationChip(message = it, modifier = Modifier.padding(horizontal = 14.dp)) }
        PixarPrimaryButton(
            text = "Swap ${GameConstants.SWAP_DUPLICATES_FOR_PACK} for 1 Pack",
            onClick = viewModel::redeemForPack,
            enabled = deck.canRedeemPack && !ui.loading,
            loading = ui.loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
        )
        if (deck.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No duplicates yet. Open packs to fill your swap deck.",
                    textAlign = TextAlign.Center,
                    color = AlbumPageStyle.bottomNavUnselectedIcon,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(deck.items, key = { it.stickerId }) { item ->
                    SwapDeckCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun SwapDeckCard(item: SwapDeckItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AlbumPageStyle.filterUnselectedFill)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient)),
            contentAlignment = Alignment.TopEnd,
        ) {
            if (item.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(
                text = "×${item.duplicateCount}",
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        AlbumPageStyle.headerAccent.copy(alpha = 0.9f),
                        RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = AlbumPageStyle.bottomNavUnselectedLabel,
        )
    }
}
