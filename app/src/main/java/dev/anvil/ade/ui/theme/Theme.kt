package dev.anvil.ade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
// ThemeEngine → Material3 color scheme mapping
// ═══════════════════════════════════════════════════════════

/** Maps a [ThemeTokens] set to a Material3 [darkColorScheme]. */
private fun tokensToDarkScheme(t: ThemeTokens) = darkColorScheme(
    primary = t.accent,
    onPrimary = t.accentInk,
    primaryContainer = t.accentBg,
    onPrimaryContainer = t.accent,
    secondary = t.ink2,
    onSecondary = t.bg,
    secondaryContainer = t.surface,
    onSecondaryContainer = t.ink,
    tertiary = t.ink3,
    onTertiary = t.bg,
    tertiaryContainer = t.surface,
    onTertiaryContainer = t.ink2,
    error = t.err,
    onError = t.ink,
    errorContainer = t.diffDelBg,
    onErrorContainer = t.err,
    background = t.bg,
    onBackground = t.ink,
    surface = t.surface,
    onSurface = t.ink,
    surfaceVariant = t.surface,
    onSurfaceVariant = t.ink2,
    outline = t.border,
    outlineVariant = t.border,
    surfaceContainerLowest = t.bg,
    surfaceContainerLow = t.surface,
    surfaceContainer = t.surface,
    surfaceContainerHigh = t.surface,
    surfaceContainerHighest = t.surface,
)

/** Legacy Vercel dark scheme — preserved for VERCEL engine option. */
private val VercelDarkScheme = darkColorScheme(
    primary = Color(VercelBlue), onPrimary = Color(VcOnErrorDark),
    primaryContainer = Color(0xFF0070F3), onPrimaryContainer = Color(0xFFEDEDED),
    secondary = Color(0xFFA1A1A1), onSecondary = Color(VcBackgroundDark),
    secondaryContainer = Color(0xFF1A1A1A), onSecondaryContainer = Color(0xFFEDEDED),
    tertiary = Color(0xFFA1A1A1), onTertiary = Color(VcBackgroundDark),
    tertiaryContainer = Color(0xFF1A1A1A), onTertiaryContainer = Color(0xFFEDEDED),
    error = Color(VcErrorDark), onError = Color(VcOnErrorDark),
    errorContainer = Color(0xFF331111), onErrorContainer = Color(0xFFFF5B4F),
    background = Color(VcBackgroundDark), onBackground = Color(VcOnBackgroundDark),
    surface = Color(VcSurfaceDark), onSurface = Color(VcOnSurfaceDark),
    surfaceVariant = Color(VcSurfaceVariantDark), onSurfaceVariant = Color(VcOnSurfaceVariantDark),
    outline = Color(VcOutlineDark), outlineVariant = Color(VcOutlineVariantDark),
    surfaceContainerLowest = Color(VcSurfaceLowestDark),
    surfaceContainerLow = Color(VcSurfaceLowDark),
    surfaceContainer = Color(VcSurfaceContainerDark),
    surfaceContainerHigh = Color(VcSurfaceHighDark),
    surfaceContainerHighest = Color(VcSurfaceHighestDark),
)

/** Legacy Vercel light scheme. */
private val VercelLightScheme = lightColorScheme(
    primary = Color(VercelBlue), onPrimary = Color(VcOnErrorLight),
    primaryContainer = Color(0xFFEBF5FF), onPrimaryContainer = Color(0xFF0068D6),
    secondary = Color(0xFF666666), onSecondary = Color(VcBackgroundLight),
    secondaryContainer = Color(0xFFFAFAFA), onSecondaryContainer = Color(0xFF171717),
    tertiary = Color(0xFF666666), onTertiary = Color(VcBackgroundLight),
    tertiaryContainer = Color(0xFFFAFAFA), onTertiaryContainer = Color(0xFF171717),
    error = Color(VcErrorLight), onError = Color(VcOnErrorLight),
    errorContainer = Color(0xFFFFEBEB), onErrorContainer = Color(0xFFCC0000),
    background = Color(VcBackgroundLight), onBackground = Color(VcOnBackgroundLight),
    surface = Color(VcSurfaceLight), onSurface = Color(VcOnSurfaceLight),
    surfaceVariant = Color(VcSurfaceVariantLight), onSurfaceVariant = Color(VcOnSurfaceVariantLight),
    outline = Color(VcOutlineLight), outlineVariant = Color(VcOutlineVariantLight),
    surfaceContainerLowest = Color(VcSurfaceLowestLight),
    surfaceContainerLow = Color(VcSurfaceLowLight),
    surfaceContainer = Color(VcSurfaceContainerLight),
    surfaceContainerHigh = Color(VcSurfaceHighLight),
    surfaceContainerHighest = Color(VcSurfaceHighestLight),
)

// ═══════════════════════════════════════════════════════════
// AnvilTheme — root composable
// ═══════════════════════════════════════════════════════════

/**
 * Root theme wrapper for all Anvil screens.
 *
 * @param engine  Active [ThemeEngine]; defaults to BLUEPRINT.
 * @param content Composable tree that receives [LocalThemeTokens].
 */
@Composable
fun AnvilTheme(
    engine: ThemeEngine = ThemeEngine.BLUEPRINT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tokens: ThemeTokens = remember(engine) {
        when (engine) {
            ThemeEngine.BLUEPRINT -> BlueprintTokens
            ThemeEngine.VERCEL   -> VercelTokens
            // MONET / LINEAR — stub: reuse Blueprint untuk sekarang.
            ThemeEngine.MONET, ThemeEngine.LINEAR -> BlueprintTokens
        }
    }

    val colorScheme = remember(engine) {
        when (engine) {
            ThemeEngine.BLUEPRINT -> tokensToDarkScheme(tokens)
            ThemeEngine.VERCEL -> if (darkTheme) VercelDarkScheme else VercelLightScheme
            // MONET / LINEAR stubs: use blueprint dark scheme for now.
            ThemeEngine.MONET, ThemeEngine.LINEAR -> tokensToDarkScheme(tokens)
        }
    }

    CompositionLocalProvider(LocalThemeTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AnvilTypography,
            content = content,
        )
    }
}