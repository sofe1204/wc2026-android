package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.ui.theme.RarityCommon
import com.techmomentum.wc2026.ui.theme.RarityEpic
import com.techmomentum.wc2026.ui.theme.RarityLegendary
import com.techmomentum.wc2026.ui.theme.RarityRare

fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.COMMON -> RarityCommon
    Rarity.RARE -> RarityRare
    Rarity.EPIC -> RarityEpic
    Rarity.LEGENDARY -> RarityLegendary
}

@Composable
fun RarityBadge(rarity: Rarity, modifier: Modifier = Modifier) {
    val color = rarityColor(rarity)
    Text(
        text = rarity.name.lowercase().replaceFirstChar { it.uppercase() },
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.6f))),
                shape = RoundedCornerShape(8.dp),
            )
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
