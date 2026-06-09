package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken

private val panelShape = RoundedCornerShape(28.dp)

/** Single soft surface — one per screen section instead of many nested boxes. */
@Composable
fun CollectorPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = panelShape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.14f),
            )
            .clip(panelShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White,
                        Color(0xFFFFFCF7),
                    ),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content,
    )
}

@Composable
fun CollectorSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.headerAccent.darken(0.08f),
        )
        trailing?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent,
            )
        }
    }
}

@Composable
fun CollectorSoftDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(50))
            .background(AlbumPageStyle.filterUnselectedBorder.copy(alpha = 0.55f))
            .padding(vertical = 0.5.dp),
    )
}

/** Horizontal metric — readable row, not a square tile. */
@Composable
fun CollectorMetricRow(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (highlight) {
                    AlbumPageStyle.headerAccent.copy(alpha = 0.08f)
                } else {
                    AlbumPageStyle.filterUnselectedFill.copy(alpha = 0.65f)
                },
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AlbumPageStyle.bottomNavUnselectedLabel,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent,
        )
    }
}

/** Reward / status row with availability dot. */
@Composable
fun CollectorStatusRow(
    emoji: String,
    title: String,
    status: String,
    available: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (available) {
                    Brush.horizontalGradient(
                        listOf(
                            AlbumPageStyle.headerAccentVivid.copy(alpha = 0.92f),
                            AlbumPageStyle.headerAccent.copy(alpha = 0.88f),
                        ),
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            AlbumPageStyle.filterUnselectedFill,
                            AlbumPageStyle.filterUnselectedFill,
                        ),
                    )
                },
            )
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (available) Color.White else AlbumPageStyle.bottomNavUnselectedLabel,
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (available) {
                    Color.White.copy(alpha = 0.88f)
                } else {
                    AlbumPageStyle.bottomNavUnselectedIcon
                },
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    if (available) Color(0xFF9AE6B8) else AlbumPageStyle.filterUnselectedBorder,
                ),
        )
    }
}
