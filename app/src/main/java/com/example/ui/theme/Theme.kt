package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MonoDarkPrimary,
    onPrimary = MonoLightPrimary, // black text on white primary
    secondary = MonoDarkSecondary,
    onSecondary = MonoLightPrimary,
    background = MonoDarkBackground,
    onBackground = MonoDarkPrimary,
    surface = MonoDarkSurface,
    onSurface = MonoDarkPrimary,
    surfaceVariant = MonoDarkSurfaceVariant,
    onSurfaceVariant = MonoDarkPrimary,
    outline = MonoDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = MonoLightPrimary,
    onPrimary = MonoDarkPrimary, // white text on black primary
    secondary = MonoLightSecondary,
    onSecondary = MonoDarkPrimary,
    background = MonoLightBackground,
    onBackground = MonoLightPrimary,
    surface = MonoLightSurface,
    onSurface = MonoLightPrimary,
    surfaceVariant = MonoLightSurfaceVariant,
    onSurfaceVariant = MonoLightPrimary,
    outline = MonoLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
