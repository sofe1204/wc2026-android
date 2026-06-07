package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken

@Composable
fun AlbumEmptyFilterState(
    modifier: Modifier = Modifier,
    isSearchActive: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        AlbumPageStyle.filterUnselectedFill,
                        AlbumPageStyle.pageFrameGradient.last(),
                    ),
                ),
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🔍",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = if (isSearchActive) "No nations found" else "No teams match this filter",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.headerAccent.darken(0.15f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = if (isSearchActive) {
                "Try a different spelling or clear the search."
            } else {
                "Try All, another group, or a different collection filter."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = AlbumPageStyle.headerAccent.darken(0.15f).copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
