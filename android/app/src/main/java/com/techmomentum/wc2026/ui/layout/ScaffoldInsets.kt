package com.techmomentum.wc2026.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalMainScaffoldPadding = compositionLocalOf { PaddingValues(0.dp) }

val NoWindowInsets = WindowInsets(0, 0, 0, 0)
