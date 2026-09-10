package dev.anvil.ade.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.anvil.ade.R

/** Inter Display (system-like, clean). Falls back to Geist if not bundled. */
val InterDisplay = FontFamily(
  Font(R.font.geist_regular, weight = FontWeight.Normal),
  Font(R.font.geist_medium, weight = FontWeight.Medium),
  Font(R.font.geist_semibold, weight = FontWeight.SemiBold),
)

/** JetBrains Mono for code. */
val JetBrainsMono = FontFamily(
  Font(R.font.geistmono_regular, weight = FontWeight.Normal),
  Font(R.font.geistmono_medium, weight = FontWeight.Medium),
)

/**
 * ACS typography. Warm, readable hierarchy.
 * Display sizes for the "ACS" logo mark.
 * Mono for code paths, tech labels, IDE chrome.
 */
val AnvilTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.SemiBold,
    fontSize = 38.sp, lineHeight = 42.sp,
    letterSpacing = (-1.0).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp, lineHeight = 38.sp,
    letterSpacing = (-0.8).sp,
  ),
  displaySmall = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp, lineHeight = 32.sp,
    letterSpacing = (-0.6).sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp, lineHeight = 28.sp,
    letterSpacing = (-0.5).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.SemiBold,
    fontSize = 19.sp, lineHeight = 25.sp,
    letterSpacing = (-0.4).sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.Medium,
    fontSize = 17.sp, lineHeight = 23.sp,
    letterSpacing = (-0.3).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp, lineHeight = 21.sp,
    letterSpacing = (-0.2).sp,
  ),
  titleMedium = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp, lineHeight = 20.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp, lineHeight = 16.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp, lineHeight = 24.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp, lineHeight = 21.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp, lineHeight = 18.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = InterDisplay,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp, lineHeight = 18.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp, lineHeight = 14.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp, lineHeight = 13.sp,
  ),
)