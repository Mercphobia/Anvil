package dev.anvil.ade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AcsDarkColorScheme = darkColorScheme(
  primary = AcsGold,
  onPrimary = AcsBg,
  primaryContainer = AcsGoldDark,
  onPrimaryContainer = AcsOnBg,
  secondary = AcsTeal,
  onSecondary = AcsBg,
  secondaryContainer = Color(0xFF1A2E2B),
  onSecondaryContainer = AcsTeal,
  tertiary = AcsTealDark,
  onTertiary = AcsBg,
  tertiaryContainer = Color(0xFF1A2E2B),
  onTertiaryContainer = AcsTeal,
  error = AcsError,
  onError = AcsOnError,
  errorContainer = AcsErrorContainer,
  onErrorContainer = AcsOnErrorContainer,
  background = AcsBg,
  onBackground = AcsOnBg,
  surface = AcsSurface0,
  onSurface = AcsOnSurface,
  surfaceVariant = AcsSurface1,
  onSurfaceVariant = AcsOnSurfaceVariant,
  outline = AcsOutline,
  outlineVariant = AcsOutlineVariant,
  surfaceContainerLowest = Color(0xFF080605),
  surfaceContainerLow = AcsSurface0,
  surfaceContainer = AcsSurface2,
  surfaceContainerHigh = AcsSurface3,
  surfaceContainerHighest = AcsSurface4,
)

private val AcsLightColorScheme = lightColorScheme(
  primary = AcsGoldDark,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFF5ECD7),
  onPrimaryContainer = Color(0xFF5C3D0E),
  secondary = AcsTealDark,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE0F7F5),
  onSecondaryContainer = Color(0xFF134E48),
  tertiary = AcsTealDark,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFE0F7F5),
  onTertiaryContainer = Color(0xFF134E48),
  error = Color(0xFFCC0000),
  onError = Color.White,
  errorContainer = Color(0xFFFFEBEB),
  onErrorContainer = Color(0xFFCC0000),
  background = AcsBgLight,
  onBackground = AcsOnBgLight,
  surface = Color.White,
  onSurface = AcsOnBgLight,
  surfaceVariant = Color(0xFFF5F5F5),
  onSurfaceVariant = Color(0xFF666666),
  outline = Color(0xFFD1D1D1),
  outlineVariant = Color(0xFFEBEBEB),
  surfaceContainerLowest = Color.White,
  surfaceContainerLow = Color(0xFFFAFAFA),
  surfaceContainer = Color(0xFFF5F5F5),
  surfaceContainerHigh = Color(0xFFEBEBEB),
  surfaceContainerHighest = Color(0xFFE5E5E5),
)

@Composable
fun AnvilTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) AcsDarkColorScheme else AcsLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = AnvilTypography,
    content = content,
  )
}