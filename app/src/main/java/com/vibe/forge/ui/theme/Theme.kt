package com.vibe.forge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ForgeColorScheme = darkColorScheme(
    primary = ForgePrimary,
    onPrimary = ForgeOnPrimary,
    background = ForgeBackground,
    onBackground = ForgeText,
    surface = ForgeSurface,
    onSurface = ForgeText,
    surfaceVariant = ForgeSurfaceVariant,
    onSurfaceVariant = ForgeTextDim,
    outline = ForgeBorder,
    secondary = ForgeAccent,
    error = ForgeError
)

@Composable
fun VibeForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ForgeColorScheme,
        content = content
    )
}
