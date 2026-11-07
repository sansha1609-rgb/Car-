package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class CockpitTheme(val displayName: String, val primaryColor: Color, val accentColor: Color) {
    STANDARD("Monochrome Black & White", Color(0xFFFFFFFF), Color(0xFFCCCCCC))
}

enum class CarWallpaper(val displayName: String, val startColor: Color, val endColor: Color, val accentTint: Color) {
    OLED_BLACK("OLED Deep Black", Color(0xFF000000), Color(0xFF000000), Color(0x18FFFFFF)),
    DARK_CARBON("Dark Carbon", Color(0xFF0A0A0A), Color(0xFF141414), Color(0x22FFFFFF)),
    CLEAN_LIGHT("Clean Light", Color(0xFF121212), Color(0xFF1C1C1C), Color(0x22FFFFFF))
}

@Composable
fun CelerioAutoTheme(
    isNightMode: Boolean = true,
    cockpitTheme: CockpitTheme = CockpitTheme.STANDARD,
    content: @Composable () -> Unit
) {
    // High-contrast, clean dark UI with a pure black and white color scheme
    val colorScheme = darkColorScheme(
        primary = Color(0xFFFFFFFF),
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFFE5E5E5),
        onSecondary = Color(0xFF000000),
        tertiary = Color(0xFFB3B3B3),
        onTertiary = Color(0xFF000000),
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF0D0D0D),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFFE0E0E0),
        outline = Color(0xFF2E2E2E),
        outlineVariant = Color(0xFF1F1F1F),
        error = Color(0xFFFFFFFF),
        onError = Color(0xFF000000)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
