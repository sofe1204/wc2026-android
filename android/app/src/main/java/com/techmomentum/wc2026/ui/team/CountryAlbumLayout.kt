package com.techmomentum.wc2026.ui.team

import androidx.compose.ui.unit.dp

object CountryAlbumLayout {
    // Two big collectible pockets dominate the screen.
    const val GRID_COLUMNS = 2

    // Aspect ratio (width / height) of the sticker art area; < 1 means a tall, portrait pocket.
    const val SLOT_ASPECT_RATIO = 0.82f

    /** Wide crest slot above the player grid. */
    const val CREST_SLOT_ASPECT_RATIO = 1.65f
    const val crestSlotWidthFraction = 0.72f

    val pagePadding = 16.dp
    val gridSpacing = 14.dp
    val pageCornerRadius = 30.dp

    // Compact banner header.
    val bannerEmblemSize = 64.dp
    val heroProgressSize = 62.dp

    // Pockets.
    val slotCornerRadius = 22.dp
    val glossHeight = 30.dp
}
