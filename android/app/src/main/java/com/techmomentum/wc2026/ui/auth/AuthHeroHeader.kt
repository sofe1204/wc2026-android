package com.techmomentum.wc2026.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.components.AppLogo

@Composable
fun AuthHeroHeader(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        AppLogo(
            modifier = Modifier.padding(vertical = 8.dp),
            size = 180.dp,
        )
    }
}
