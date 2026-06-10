package com.techmomentum.wc2026.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.techmomentum.wc2026.ui.components.dismissKeyboardOnTap

@Composable
fun TopBarEdgeToEdgeScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    background: @Composable BoxScope.() -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val mainPadding = LocalMainScaffoldPadding.current
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        containerColor = Color.Transparent,
        contentWindowInsets = NoWindowInsets,
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                background()
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(PaddingValues(bottom = mainPadding.calculateBottomPadding()))
                    .dismissKeyboardOnTap(),
            ) {
                content()
            }
            overlay()
        }
    }
}
