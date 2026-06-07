package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.R

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    Image(
        painter = painterResource(R.drawable.logo_launcher),
        contentDescription = "Anime World Cup Sticker Album",
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
}
