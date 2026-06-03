package com.techmomentum.wc2026.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.techmomentum.wc2026.ui.theme.TeamPalette

@Composable
fun CountryAlbumBackgroundOrbs(palette: TeamPalette, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.accentVivid.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(140f, 160f),
                        radius = 820f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.accent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(960f, 1400f),
                        radius = 720f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.primary.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(120f, 1700f),
                        radius = 620f,
                    ),
                ),
        )
    }
}
