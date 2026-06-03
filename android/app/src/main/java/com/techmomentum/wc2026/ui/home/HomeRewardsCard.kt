package com.techmomentum.wc2026.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.components.PixarStatusChip
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeRewardsCard(
    dailyClaimedToday: Boolean,
    adPackClaimedToday: Boolean,
    slotSpinsRemaining: Int,
    slotPacksWonToday: Int,
    slotPackCap: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.2f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Daily rewards",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent.darken(0.1f),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixarStatusChip(
                label = "Daily packs",
                available = !dailyClaimedToday,
            )
            PixarStatusChip(
                label = "Ad bonus",
                available = !adPackClaimedToday,
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixarStatusChip(
                label = "Slot spins · $slotSpinsRemaining",
                available = slotSpinsRemaining > 0,
            )
            PixarStatusChip(
                label = "Slot packs · $slotPacksWonToday/$slotPackCap",
                available = slotPacksWonToday < slotPackCap,
            )
        }
    }
}
