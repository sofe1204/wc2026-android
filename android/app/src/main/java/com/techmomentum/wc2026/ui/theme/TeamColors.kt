package com.techmomentum.wc2026.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.techmomentum.wc2026.data.model.Team

/**
 * Vibrant-national palette: real flag colors are saturation-boosted and used as the hero/background
 * gradients. White (or other near-white secondaries) are intentionally NOT used as large fills —
 * only as small gloss/highlight accents — so teams like Mexico/Argentina/France stay richly colored.
 */
data class TeamPalette(
    val primary: Color,
    val secondary: Color,
    val onGradient: Color,
    val scrim: Color,
    val heroGradient: List<Color>,
    val backgroundGradient: List<Color>,
    val cardGradient: List<Color>,
    val sheetColor: Color,
    val accent: Color,
    val accentVivid: Color,
)

fun parseTeamColor(hex: String, fallback: Color): Color =
    runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)

fun Color.lighten(factor: Float): Color = Color(
    red = (red + (1f - red) * factor).coerceIn(0f, 1f),
    green = (green + (1f - green) * factor).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
    alpha = alpha,
)

fun Color.darken(factor: Float): Color = Color(
    red = (red * (1f - factor)).coerceIn(0f, 1f),
    green = (green * (1f - factor)).coerceIn(0f, 1f),
    blue = (blue * (1f - factor)).coerceIn(0f, 1f),
    alpha = alpha,
)

fun Color.saturate(factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv)).copy(alpha = alpha)
}

@Composable
fun teamPalette(team: Team): TeamPalette {
    val fallbackPrimary = MaterialTheme.colorScheme.primary
    val fallbackSecondary = MaterialTheme.colorScheme.secondary
    val rawPrimary = parseTeamColor(team.primaryColor, fallbackPrimary)
    val rawSecondary = parseTeamColor(team.secondaryColor, fallbackSecondary)

    // Boost saturation so flag colors read as vivid/Pixar, not muted.
    val primary = rawPrimary.saturate(1.25f)
    val secondaryLight = rawSecondary.luminance() > 0.82f

    // Second vivid accent: use a real colorful secondary if available, otherwise a brighter
    // shade of the primary (so white-secondary teams never wash out).
    val accentVivid = if (secondaryLight) {
        primary.saturate(1.1f).lighten(0.28f)
    } else {
        rawSecondary.saturate(1.2f)
    }

    val onGradient = if (primary.luminance() > 0.55f) Color(0xFF12202E) else Color.White

    return TeamPalette(
        primary = primary,
        secondary = rawSecondary,
        onGradient = onGradient,
        scrim = Color.Black.copy(alpha = 0.06f),
        // Compact banner: bright, friendly, team-branded (no dark top).
        heroGradient = listOf(
            primary.lighten(0.12f),
            primary,
            accentVivid,
        ),
        // Warm cream "album paper" with only a faint team tint, so the page reads
        // as a bright premium album rather than a single-color dashboard.
        backgroundGradient = listOf(
            Color(0xFFFFFDF8),
            primary.lighten(0.86f),
            Color(0xFFFFF3E6),
        ),
        // Glossy pocket: white top fading to a soft team tint.
        cardGradient = listOf(
            Color.White,
            primary.lighten(0.8f),
        ),
        sheetColor = primary.lighten(0.92f),
        accent = CardGold,
        accentVivid = accentVivid,
    )
}

fun positionAbbrev(position: String): String = when {
    position.contains("Goal", ignoreCase = true) -> "GK"
    position.contains("Def", ignoreCase = true) -> "DEF"
    position.contains("Mid", ignoreCase = true) -> "MID"
    position.contains("For", ignoreCase = true) -> "FWD"
    else -> position.take(3).uppercase()
}
