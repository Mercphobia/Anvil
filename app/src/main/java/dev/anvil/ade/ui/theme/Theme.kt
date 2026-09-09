package dev.anvil.ade.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val MonetDarkColorScheme = darkColorScheme(
  primary = MonetPrimaryDark,
  onPrimary = MonetOnPrimaryDark,
  primaryContainer = MonetPrimaryContainerDark,
  onPrimaryContainer = MonetOnPrimaryContainerDark,
  secondary = MonetSecondaryDark,
  onSecondary = MonetOnSecondaryDark,
  secondaryContainer = MonetSecondaryContainerDark,
  onSecondaryContainer = MonetOnSecondaryContainerDark,
  tertiary = MonetTertiaryDark,
  onTertiary = MonetOnTertiaryDark,
  tertiaryContainer = MonetTertiaryContainerDark,
  onTertiaryContainer = MonetOnTertiaryContainerDark,
  error = MonetErrorDark,
  onError = MonetOnErrorDark,
  errorContainer = MonetErrorContainerDark,
  onErrorContainer = MonetOnErrorContainerDark,
  background = MonetBackgroundDark,
  onBackground = MonetOnBackgroundDark,
  surface = MonetSurfaceDark,
  onSurface = MonetOnSurfaceDark,
  surfaceVariant = MonetSurfaceVariantDark,
  onSurfaceVariant = MonetOnSurfaceVariantDark,
  outline = MonetOutlineDark,
  outlineVariant = MonetOutlineVariantDark,
  surfaceContainerLowest = MonetSurfaceContainerLowestDark,
  surfaceContainerLow = MonetSurfaceContainerLowDark,
  surfaceContainer = MonetSurfaceContainerDark,
  surfaceContainerHigh = MonetSurfaceContainerHighDark,
  surfaceContainerHighest = MonetSurfaceContainerHighestDark,
)

private val MonetLightColorScheme = lightColorScheme(
  primary = MonetPrimaryLight,
  onPrimary = MonetOnPrimaryLight,
  primaryContainer = MonetPrimaryContainerLight,
  onPrimaryContainer = MonetOnPrimaryContainerLight,
  secondary = MonetSecondaryLight,
  onSecondary = MonetOnSecondaryLight,
  secondaryContainer = MonetSecondaryContainerLight,
  onSecondaryContainer = MonetOnSecondaryContainerLight,
  tertiary = MonetTertiaryLight,
  onTertiary = MonetOnTertiaryLight,
  tertiaryContainer = MonetTertiaryContainerLight,
  onTertiaryContainer = MonetOnTertiaryContainerLight,
  error = MonetErrorLight,
  onError = MonetOnErrorLight,
  errorContainer = MonetErrorContainerLight,
  onErrorContainer = MonetOnErrorContainerLight,
  background = MonetBackgroundLight,
  onBackground = MonetOnBackgroundLight,
  surface = MonetSurfaceLight,
  onSurface = MonetOnSurfaceLight,
  surfaceVariant = MonetSurfaceVariantLight,
  onSurfaceVariant = MonetOnSurfaceVariantLight,
  outline = MonetOutlineLight,
  outlineVariant = MonetOutlineVariantLight,
  surfaceContainerLowest = MonetSurfaceContainerLowestLight,
  surfaceContainerLow = MonetSurfaceContainerLowLight,
  surfaceContainer = MonetSurfaceContainerLight,
  surfaceContainerHigh = MonetSurfaceContainerHighLight,
  surfaceContainerHighest = MonetSurfaceContainerHighestLight,
)

@Composable
fun AnvilTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Monet Dynamic Color support on Android 12+ (API 31+)
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> MonetDarkColorScheme
    else -> MonetLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = ExpressiveTypography,
    content = content,
  )
}
