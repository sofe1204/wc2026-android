package com.techmomentum.wc2026.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = AnimeCyan,
    secondary = AnimePink,
    tertiary = CardGold,
    background = StadiumNight,
    surface = Color(0xFF1A2F45),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColors = lightColorScheme(
    primary = PitchGreen,
    secondary = AnimePink,
    tertiary = CardGold,
    background = Color(0xFFF0F8FF),
    surface = Color.White,
    onPrimary = Color.White,
)

@Composable
fun WorldCup2026Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
