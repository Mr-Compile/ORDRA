package com.ordra.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val OrdraLightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E4FF),
    onPrimaryContainer = Color(0xFF14164D),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAE4FF),
    onSecondaryContainer = Color(0xFF261457),
    tertiary = OrdraMint,
    onTertiary = Color(0xFF06382A),
    tertiaryContainer = Color(0xFFD2F5E9),
    onTertiaryContainer = Color(0xFF06382A),
    error = OrdraError,
    onError = Color.White,
    errorContainer = Color(0xFFFDD9D9),
    onErrorContainer = Color(0xFF5C0B0B),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = Color(0xFFE8E9F2),
)

internal val OrdraDarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF14164D),
    primaryContainer = OrdraDeepIndigo,
    onPrimaryContainer = Color(0xFFE3E4FF),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF261457),
    secondaryContainer = Color(0xFF4A3A85),
    onSecondaryContainer = Color(0xFFEAE4FF),
    tertiary = OrdraMint,
    onTertiary = Color(0xFF06382A),
    tertiaryContainer = Color(0xFF0E5A43),
    onTertiaryContainer = Color(0xFFD2F5E9),
    error = Color(0xFFF88A8A),
    onError = Color(0xFF5C0B0B),
    errorContainer = Color(0xFF8A1E1E),
    onErrorContainer = Color(0xFFFDD9D9),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = Color(0xFF26272F),
)

@Composable
fun OrdraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) OrdraDarkColors else OrdraLightColors,
        typography = OrdraTypography,
        content = content,
    )
}
