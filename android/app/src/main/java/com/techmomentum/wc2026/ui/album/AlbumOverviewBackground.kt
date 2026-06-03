package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun AlbumOverviewBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF5BC48A).copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(120f, 180f),
                        radius = 700f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF6B9BD1).copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(900f, 1200f),
                        radius = 760f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(AlbumPageStyle.headerAccent.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(80f, 1600f),
                        radius = 620f,
                    ),
                ),
        )
    }
}
