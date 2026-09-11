package dev.anvil.ade.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
// Theme Engine selector
// ═══════════════════════════════════════════════════════════

/**
 * Available visual themes in Anvil.
 * BLUEPRINT is the default for new installs.
 * VERCEL, MONET, LINEAR remain as optional choices.
 */
enum class ThemeEngine { BLUEPRINT, VERCEL, MONET, LINEAR }

// ═══════════════════════════════════════════════════════════
// Vercel Geist palette (preserved for VERCEL engine option)
// ═══════════════════════════════════════════════════════════

val VercelBlue = 0xFF0070F3

// Dark scheme colors
val VcBackgroundDark = 0xFF0A0A0A
val VcOnBackgroundDark = 0xFFEDEDED
val VcSurfaceDark = 0xFF0A0A0A
val VcOnSurfaceDark = 0xFFEDEDED
val VcSurfaceVariantDark = 0xFF1A1A1A
val VcOnSurfaceVariantDark = 0xFFA1A1A1
val VcOutlineDark = 0xFF333333
val VcOutlineVariantDark = 0xFF262626
val VcSurfaceLowestDark = 0xFF000000
val VcSurfaceLowDark = 0xFF111111
val VcSurfaceContainerDark = 0xFF161616
val VcSurfaceHighDark = 0xFF1A1A1A
val VcSurfaceHighestDark = 0xFF222222
val VcErrorDark = 0xFFFF5B4F
val VcOnErrorDark = 0xFF0A0A0A

// Light scheme colors
val VcBackgroundLight = 0xFFFFFFFF
val VcOnBackgroundLight = 0xFF171717
val VcSurfaceLight = 0xFFFFFFFF
val VcOnSurfaceLight = 0xFF171717
val VcSurfaceVariantLight = 0xFFFAFAFA
val VcOnSurfaceVariantLight = 0xFF666666
val VcOutlineLight = 0xFFA3A3A3
val VcOutlineVariantLight = 0xFFEBEBEB
val VcSurfaceLowestLight = 0xFFFFFFFF
val VcSurfaceLowLight = 0xFFFAFAFA
val VcSurfaceContainerLight = 0xFFF5F5F5
val VcSurfaceHighLight = 0xFFEBEBEB
val VcSurfaceHighestLight = 0xFFE5E5E5
val VcErrorLight = 0xFFEE0000
val VcOnErrorLight = 0xFFFFFFFF

// ═══════════════════════════════════════════════════════════
// REMOVED: Acs* tokens (ACS screenshot literal colors)
// REMOVED: Forge* tokens (legacy, replaced by Blueprint diff tints)
//
// All UI that previously used Acs*/Forge* now reads from
// MaterialTheme.colorScheme (mapped from the active ThemeEngine)
// or LocalThemeTokens directly.
// ═══════════════════════════════════════════════════════════