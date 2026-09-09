package dev.anvil.ade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Vercel Geist dark scheme: near-black #0A0A0A canvas, #EDEDED ink,
 * single blue accent #0070F3. Depth via 1px outlineVariant borders,
 * never elevation.
 */
private val VercelDarkColorScheme = darkColorScheme(
  primary = Color(VercelBlue),
  onPrimary = Color(VcOnErrorDark),
  primaryContainer = Color(0xFF0070F3),
  onPrimaryContainer = Color(0xFFEDEDED),
  secondary = Color(0xFFA1A1A1),
  onSecondary = Color(VcBackgroundDark),
  secondaryContainer = Color(0xFF1A1A1A),
  onSecondaryContainer = Color(0xFFEDEDED),
  tertiary = Color(0xFFA1A1A1),
  onTertiary = Color(VcBackgroundDark),
  tertiaryContainer = Color(0xFF1A1A1A),
  onTertiaryContainer = Color(0xFFEDEDED),
  error = Color(VcErrorDark),
  onError = Color(VcOnErrorDark),
  errorContainer = Color(0xFF331111),
  onErrorContainer = Color(0xFFFF5B4F),
  background = Color(VcBackgroundDark),
  onBackground = Color(VcOnBackgroundDark),
  surface = Color(VcSurfaceDark),
  onSurface = Color(VcOnSurfaceDark),
  surfaceVariant = Color(VcSurfaceVariantDark),
  onSurfaceVariant = Color(VcOnSurfaceVariantDark),
  outline = Color(VcOutlineDark),
  outlineVariant = Color(VcOutlineVariantDark),
  surfaceContainerLowest = Color(VcSurfaceLowestDark),
  surfaceContainerLow = Color(VcSurfaceLowDark),
  surfaceContainer = Color(VcSurfaceContainerDark),
  surfaceContainerHigh = Color(VcSurfaceHighDark),
  surfaceContainerHighest = Color(VcSurfaceHighestDark),
)

/**
 * Vercel Geist light scheme: pure white canvas, #171717 ink,
 * single blue accent. Micro-warmth: #171717 not #000000.
 */
private val VercelLightColorScheme = lightColorScheme(
  primary = Color(VercelBlue),
  onPrimary = Color(VcOnErrorLight),
  primaryContainer = Color(0xFFEBF5FF),
  onPrimaryContainer = Color(0xFF0068D6),
  secondary = Color(0xFF666666),
  onSecondary = Color(VcBackgroundLight),
  secondaryContainer = Color(0xFFFAFAFA),
  onSecondaryContainer = Color(0xFF171717),
  tertiary = Color(0xFF666666),
  onTertiary = Color(VcBackgroundLight),
  tertiaryContainer = Color(0xFFFAFAFA),
  onTertiaryContainer = Color(0xFF171717),
  error = Color(VcErrorLight),
  onError = Color(VcOnErrorLight),
  errorContainer = Color(0xFFFFEBEB),
  onErrorContainer = Color(0xFFCC0000),
  background = Color(VcBackgroundLight),
  onBackground = Color(VcOnBackgroundLight),
  surface = Color(VcSurfaceLight),
  onSurface = Color(VcOnSurfaceLight),
  surfaceVariant = Color(VcSurfaceVariantLight),
  onSurfaceVariant = Color(VcOnSurfaceVariantLight),
  outline = Color(VcOutlineLight),
  outlineVariant = Color(VcOutlineVariantLight),
  surfaceContainerLowest = Color(VcSurfaceLowestLight),
  surfaceContainerLow = Color(VcSurfaceLowLight),
  surfaceContainer = Color(VcSurfaceContainerLight),
  surfaceContainerHigh = Color(VcSurfaceHighLight),
  surfaceContainerHighest = Color(VcSurfaceHighestLight),
)

@Composable
fun AnvilTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) VercelDarkColorScheme else VercelLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AnvilTypography,
    content = content,
  )
}
