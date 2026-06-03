package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.team.CountryAlbumLayout
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun AlbumPageFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(CountryAlbumLayout.pageCornerRadius))
            .clip(RoundedCornerShape(CountryAlbumLayout.pageCornerRadius))
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient)),
    ) {
        content()
    }
}
