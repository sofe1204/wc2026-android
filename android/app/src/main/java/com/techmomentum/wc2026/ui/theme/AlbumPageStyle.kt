package com.techmomentum.wc2026.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Neutral “album overview” palette for My Album (not tied to one nation). */
object AlbumPageStyle {
    val backgroundGradient = listOf(
        Color(0xFFE6F4EC),
        Color(0xFFFFF9F2),
        Color(0xFFE8EEF8),
    )

    val pageFrameGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFFBF4),
    )

    val headerAccent = Color(0xFF1B6B4A)
    val headerAccentVivid = Color(0xFF2E9B6A)
    val filterSelectedStart = Color(0xFF2A8F5E)
    val filterSelectedEnd = Color(0xFF5BC48A)
    val filterUnselectedFill = Color(0xFFF5F8F6)
    val filterUnselectedBorder = Color(0xFFD8E6DE)

    val overallProgressFill = Brush.horizontalGradient(
        listOf(Color(0xFFFFD54F), Color(0xFF5BC48A), Color(0xFF2A8F5E)),
    )

    val overallProgressTrackOnHeader = Color.White.copy(alpha = 0.28f)

    val bottomBarGradient = listOf(
        Color(0xFFFFFBF7),
        Color(0xFFF5F8F6),
    )

    val bottomNavSelectedBrush = Brush.horizontalGradient(
        listOf(filterSelectedStart, filterSelectedEnd),
    )

    val bottomNavUnselectedLabel = Color(0xFF2D4A3A)
    val bottomNavUnselectedIcon = Color(0xFF4A6B58)
}
