package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold

@Composable
fun PackCard(count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(AnimePink, CardGold)))
            .padding(16.dp),
    ) {
        Text("Sticker Packs", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
        Text("$count unopened", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
    }
}
