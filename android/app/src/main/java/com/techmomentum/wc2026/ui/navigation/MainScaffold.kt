package com.techmomentum.wc2026.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.techmomentum.wc2026.ui.components.BannerAd
import com.techmomentum.wc2026.ui.components.MainBottomBar

@Composable
fun MainScaffold(
    currentRoute: String?,
    showBottomBar: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                Column {
                    BannerAd()
                    MainBottomBar(currentRoute = currentRoute, onNavigate = onNavigate)
                }
            }
        },
        content = content,
    )
}
