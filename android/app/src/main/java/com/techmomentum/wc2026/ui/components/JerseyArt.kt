package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.lighten

/** Jersey placeholder with the shirt number printed on the chest. */
@Composable
fun JerseyArt(
    shirtNumber: Int?,
    teamColor: Color,
    accent: Color,
    owned: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight(0.78f)
            .aspectRatio(0.95f),
        contentAlignment = Alignment.Center,
    ) {
        JerseySilhouette(
            fill = Brush.verticalGradient(
                if (owned) {
                    listOf(teamColor.lighten(0.1f), teamColor, accent)
                } else {
                    listOf(teamColor.copy(alpha = 0.4f), teamColor.copy(alpha = 0.28f))
                },
            ),
            modifier = Modifier.fillMaxSize(),
            outline = Color.White.copy(alpha = if (owned) 0.8f else 0.55f),
        )
        shirtNumber?.let {
            Text(
                text = "$it",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = if (owned) 0.95f else 0.85f),
            )
        }
    }
}
