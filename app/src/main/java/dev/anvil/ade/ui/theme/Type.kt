package dev.anvil.ade.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.anvil.ade.R

/** Geist Sans (Vercel) - bundled static weights 400/500/600. */
val GeistSans = FontFamily(
  Font(R.font.geist_regular, weight = FontWeight.Normal),
  Font(R.font.geist_medium, weight = FontWeight.Medium),
  Font(R.font.geist_semibold, weight = FontWeight.SemiBold),
)

/** Geist Mono (Vercel) - bundled static weights 400/500. */
val GeistMono = FontFamily(
  Font(R.font.geistmono_regular, weight = FontWeight.Normal),
  Font(R.font.geistmono_medium, weight = FontWeight.Medium),
)

/**
 * Vercel Geist typography. Three weights only: 400 (read), 500 (interact),
 * 600 (announce). Negative letter-spacing at display sizes (compression as
 * identity). Geist Mono for technical labels (via FontFamily.Monospace -
 * closest system approximation; Geist fonts can be bundled later).
 */
val AnvilTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 38.sp, lineHeight = 42.sp,
    letterSpacing = (-1.2).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp, lineHeight = 38.sp,
    letterSpacing = (-1.0).sp,
  ),
  displaySmall = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp, lineHeight = 32.sp,
    letterSpacing = (-0.8).sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp, lineHeight = 30.sp,
    letterSpacing = (-0.96).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp, lineHeight = 26.sp,
    letterSpacing = (-0.5).sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp, lineHeight = 24.sp,
    letterSpacing = (-0.4).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp, lineHeight = 22.sp,
    letterSpacing = (-0.32).sp,
  ),
  titleMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp, lineHeight = 20.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp, lineHeight = 16.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp, lineHeight = 24.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp, lineHeight = 21.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp, lineHeight = 18.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp, lineHeight = 20.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp, lineHeight = 14.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp, lineHeight = 13.sp,
  ),
)
