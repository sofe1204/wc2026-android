package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun PixarStatusChip(
    label: String,
    available: Boolean,
    modifier: Modifier = Modifier,
) {
    val statusText = if (available) "○ Available" else "✓ Claimed"
    Text(
        text = "$label · $statusText",
        modifier = modifier
            .shadow(
                elevation = if (available) 2.dp else 0.dp,
                shape = RoundedCornerShape(50),
                ambientColor = AlbumPageStyle.filterSelectedStart.copy(alpha = 0.3f),
            )
            .clip(RoundedCornerShape(50))
            .then(
                if (available) {
                    Modifier.background(
                        brush = AlbumPageStyle.bottomNavSelectedBrush,
                        shape = RoundedCornerShape(50),
                    )
                } else {
                    Modifier
                        .background(AlbumPageStyle.filterUnselectedFill, RoundedCornerShape(50))
                        .border(1.dp, AlbumPageStyle.filterUnselectedBorder, RoundedCornerShape(50))
                },
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
        ),
        color = if (available) Color.White else AlbumPageStyle.bottomNavUnselectedLabel,
    )
}
