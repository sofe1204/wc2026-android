package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun ProfileStatChip(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val shape = RoundedCornerShape(16.dp)
    val background = if (highlighted) {
        Brush.verticalGradient(
            listOf(
                AlbumPageStyle.headerAccentVivid.copy(alpha = 0.18f),
                AlbumPageStyle.headerAccent.copy(alpha = 0.1f),
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                AlbumPageStyle.filterUnselectedFill,
                AlbumPageStyle.filterUnselectedFill,
            ),
        )
    }
    val borderColor = if (highlighted) {
        AlbumPageStyle.headerAccent.copy(alpha = 0.45f)
    } else {
        AlbumPageStyle.filterUnselectedBorder
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (highlighted) 4.dp else 2.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.filterSelectedStart.copy(alpha = 0.15f),
            )
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = if (highlighted) AlbumPageStyle.headerAccent else AlbumPageStyle.headerAccent,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlbumPageStyle.bottomNavUnselectedIcon,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}
