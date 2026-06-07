package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Full-bleed content container (no inset card, shadow, or rounded frame). */
@Composable
fun AlbumPageFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
    }
}
