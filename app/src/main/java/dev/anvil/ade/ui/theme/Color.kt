package dev.anvil.ade.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Vercel Geist palette. Achromatic system: #171717 -> #ffffff is the whole
 * identity. Blue #0070F3 (console blue / focus) is the ONLY accent.
 * Dark mode inverts: near-black canvas #0A0A0A, #EDEDED ink.
 */

// ---- Shared accent (functional only) ----
val VercelBlue = 0xFF0070F3
val VercelBlueDark = 0xFF0070F3
val LinkBlue = 0xFF0072F5
val ShipRed = 0xFFFF5B4F
val PreviewPink = 0xFFDE1D8D

// ---- Dark scheme (Vercel dark: #0A0A0A canvas family) ----
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

// ---- Light scheme (Vercel light: pure white canvas) ----
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

// ---- Semantic aliases used by feature screens (Vercel-style diffs) ----
// Typed as Color for direct Modifier.background(...) usage.
val ForgeNeonGreen = Color(0xFF50E3C2)          // success / ok accent (terminal)
val ForgeNeonCyan = Color(0xFF50C8E3)           // info accent
val ForgeTerminalBg = Color(0xFF0A0A0A)         // terminal canvas = dark canvas
val ForgeDiffAddText = Color(0xFF1DA750)        // diff added text
val ForgeDiffAddBg = Color(0xFF0E2A1C)          // diff added line bg (dark, tinted)
val ForgeDiffDelText = Color(0xFFFF5B4F)        // diff removed text (Ship Red)
val ForgeDiffDelBg = Color(0xFF2A1114)          // diff removed line bg (dark, tinted)

// ---- ACS (Android Code Studio) tokens for new UI screens ----
val AcsGold = Color(0xFFD4A843)
val AcsGoldDark = Color(0xFFB8922E)
val AcsTeal = Color(0xFF3DD6C8)
val AcsTealDark = Color(0xFF2EB5A8)
val AcsRed = Color(0xFFE0554A)
val AcsGreen = Color(0xFF4CAF50)
val AcsBg = Color(0xFF0D0B0A)
val AcsOnBg = Color(0xFFEDEDED)
val AcsOnSurface = Color(0xFFEDEDED)
val AcsOnSurfaceVariant = Color(0xFFA1A1A1)
val AcsOnSurfaceDim = Color(0xFF6B6B6B)
val AcsOutline = Color(0xFF333333)
val AcsOutlineVariant = Color(0xFF262626)
val AcsSurface0 = Color(0xFF130D09)
val AcsSurface1 = Color(0xFF141218)
val AcsSurface2 = Color(0xFF1E1B19)
val AcsSurface3 = Color(0xFF24211F)
val AcsSurface4 = Color(0xFF2A2724)
val AcsBuildBg = Color(0xFF0A0807)
val AcsBuildTabActive = Color(0xFFD4A843)
val AcsBuildTabInactive = Color(0xFF6B6B6B)
val AcsBuildText = Color(0xFFA1A1A1)
val AcsTemplateCard = Color(0xFF1A1715)
val AcsTemplateCardBorder = Color(0xFF2A2724)
val AcsSidebarBg = Color(0xFF100E0C)
val AcsFileIconKt = Color(0xFF9C6ADE)
val AcsFileIconXml = Color(0xFFE0554A)
val AcsFileIconGradle = Color(0xFF3DD6C8)
val AcsFolderIcon = Color(0xFFD4A843)
