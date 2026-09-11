package dev.anvil.ade.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Blueprint design system token set — the default theme for Anvil.
 *
 * Inspired by engineering blueprints: deep navy paper (#0B1E30),
 * amber accent (#E8A054) for interactive elements, green/red status
 * markers following real blueprint annotation conventions.
 *
 * Dark-native by design (no light variant). Amber is the ONLY
 * interactive accent; green (ok) and red (err) are functional
 * status signals used sparingly — never decorative.
 */
object BlueprintTokens : ThemeTokens {

    // ── Canvas ──────────────────────────────────────────
    override val bg      = Color(0xFF0B1E30)   // deep blueprint blue
    override val surface = Color(0xFF0E2438)   // card / panel background

    // ── Ink hierarchy ───────────────────────────────────
    override val ink      = Color(0xFFE7EEF5)  // primary text
    override val ink2     = Color(0xFF9FB4C7)  // secondary text
    override val ink3     = Color(0xFF6E8299)  // tertiary / timestamp / caption

    // ── Borders ─────────────────────────────────────────
    override val border       = Color(0x38D6E4F0)  // rgba(214,228,240,0.22)
    override val borderStrong = Color(0x66D6E4F0)  // rgba(214,228,240,0.40)

    // ── Accent (amber — the ONE interactive accent) ────
    override val accent    = Color(0xFFE8A054)   // CTA, tab active, corner mark, focus
    override val accentBg  = Color(0xFF2A2115)   // dark amber tint for badges / pills
    override val accentInk = Color(0xFF0B1E30)   // text on amber (dark-on-light contrast)

    // ── Status (functional only — never decorative) ────
    override val ok  = Color(0xFF6FBF73)  // success / approved (green)
    override val err = Color(0xFFE8685D)  // error / destructive (coral-red)

    // ── Derived: diff background tints ──────────────────
    override val diffAddBg = Color(0xFF0E2A1C)  // green-tinted diff add (dark)
    override val diffDelBg = Color(0xFF2A1114)  // red-tinted   diff del (dark)
}

/**
 * Convenience extension: returns a copy of [Color] with the given
 * [alpha] (0f..1f), preserving the RGB components.
 */
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)