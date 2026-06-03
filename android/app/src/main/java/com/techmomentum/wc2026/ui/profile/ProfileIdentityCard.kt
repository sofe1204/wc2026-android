package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.darken

@Composable
fun ProfileIdentityCard(
    displayName: String,
    email: String,
    isGuest: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val initials = profileInitials(displayName)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.2f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(AnimePink.copy(alpha = 0.5f), CardGold.copy(alpha = 0.5f))),
                shape = shape,
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, CircleShape, ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.3f))
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AlbumPageStyle.headerAccentVivid,
                            AlbumPageStyle.headerAccent,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isGuest) "🧑" else initials,
                fontSize = if (isGuest) 26.sp else 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = displayName.ifBlank { "Collector" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent.darken(0.1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AlbumPageStyle.bottomNavUnselectedIcon,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isGuest) {
                Text(
                    text = "Offline demo — progress stored on device only.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = AlbumPageStyle.bottomNavUnselectedLabel.copy(alpha = 0.75f),
                )
            }
        }
    }
}

private fun profileInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
    }
}
