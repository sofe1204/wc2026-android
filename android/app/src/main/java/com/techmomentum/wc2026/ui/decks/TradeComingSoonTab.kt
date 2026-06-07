package com.techmomentum.wc2026.ui.decks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun TradeComingSoonTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "🔄",
            fontSize = 48.sp,
        )
        Text(
            text = "Player trading",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent,
        )
        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.filterSelectedStart,
        )
        Text(
            text = "Swap duplicates with other collectors to complete your album faster.",
            style = MaterialTheme.typography.bodyMedium,
            color = AlbumPageStyle.bottomNavUnselectedIcon,
            textAlign = TextAlign.Center,
        )
    }
}
