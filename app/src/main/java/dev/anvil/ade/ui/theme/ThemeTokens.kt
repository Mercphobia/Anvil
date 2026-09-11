package dev.anvil.ade.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Contract for all theme engines in Anvil.
 * Every theme MUST provide these semantic tokens — the UI layer
 * never references raw hex values directly.
 */
interface ThemeTokens {
    // Canvas
    val bg: Color
    val surface: Color

    // Ink (text hierarchy)
    val ink: Color          // primary text
    val ink2: Color         // secondary text
    val ink3: Color         // tertiary / dim text

    // Borders
    val border: Color       // subtle hairline
    val borderStrong: Color // pronounced border

    // Accent (interactive elements only — CTAs, tabs, links, focus)
    val accent: Color
    val accentBg: Color     // tinted background for accent badges
    val accentInk: Color    // text on accent background

    // Functional status colors (NOT decorative)
    val ok: Color           // success / approved
    val err: Color          // error / destructive

    // Derived: diff background tints (10% opacity of ok/err)
    val diffAddBg: Color
    val diffDelBg: Color
}

/**
 * CompositionLocal for accessing the active theme tokens from any
 * composable without passing them through parameters.
 */
val LocalThemeTokens = staticCompositionLocalOf<ThemeTokens> {
    error("No ThemeTokens provided — wrap your root in AnvilTheme")
}