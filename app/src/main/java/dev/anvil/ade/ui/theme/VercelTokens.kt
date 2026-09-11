package dev.anvil.ade.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Vercel Geist theme token set.
 * Monochrome near-black canvas (#0A0A0A), #EDEDED ink,
 * single blue accent (#0070F3). Achromatic system — depth via
 * 1px borders, never elevation.
 */
object VercelTokens : ThemeTokens {

    override val bg      = Color(0xFF0A0A0A)
    override val surface = Color(0xFF0A0A0A)

    override val ink      = Color(0xFFEDEDED)
    override val ink2     = Color(0xFFA1A1A1)
    override val ink3     = Color(0xFF666666)

    override val border       = Color(0xFF333333)
    override val borderStrong = Color(0xFF444444)

    // Blue is the ONE accent in Vercel Geist
    override val accent    = Color(0xFF0070F3)
    override val accentBg  = Color(0xFF0D1A33)
    override val accentInk = Color(0xFFEDEDED)

    // Vercel is monochrome — no green/red status distinction
    override val ok  = Color(0xFFEDEDED)
    override val err = Color(0xFFFF5B4F)

    override val diffAddBg = Color(0xFF0E2A1C)
    override val diffDelBg = Color(0xFF2A1114)
}