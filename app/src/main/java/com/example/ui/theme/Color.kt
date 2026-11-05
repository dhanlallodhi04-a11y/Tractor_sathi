package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Color Tokens (Forest Green, Amber, Warm Cream)
// ==========================================
val WarmCreamBg = Color(0xFFF6F3EC)          // --bg: #F6F3EC (warm cream page background)
val CardWhite = Color(0xFFFFFFFF)            // --surface: #FFFFFF (cards)

val DeepForestGreen = Color(0xFF14452F)      // --primary: #14452F (deep forest green)
val ForestGreenDark = Color(0xFF0E3322)      // --primary-dark: #0E3322 (pressed / 3D bottom edge)

val AmberAccent = Color(0xFFD99A1F)          // --accent: #D99A1F (amber: selected chips, highlights)
val AmberDark = Color(0xFFB07C12)            // --accent-dark: #B07C12

// Pastel Card Tints (with border and bottom edge)
val MintTint = Color(0xFFDDEFE3)             // mint: #DDEFE3
val MintBorder = Color(0xFFBFDCCB)           // border: #BFDCCB
val MintEdge = Color(0xFFA5C9B3)             // edge: #A5C9B3

val CreamYellowTint = Color(0xFFFBEFCB)      // cream-yellow: #FBEFCB
val CreamYellowBorder = Color(0xFFEBD9A0)    // border: #EBD9A0
val CreamYellowEdge = Color(0xFFD9C382)      // edge: #D9C382

val BlushPinkTint = Color(0xFFFADDE4)        // blush-pink: #FADDE4
val BlushPinkBorder = Color(0xFFEDBBC8)      // border: #EDBBC8
val BlushPinkEdge = Color(0xFFDCA3B2)        // edge: #DCA3B2

val SkyTint = Color(0xFFDCEBF7)              // sky: #DCEBF7
val SkyBorder = Color(0xFFB9D3E8)            // border: #B9D3E8
val SkyEdge = Color(0xFFA1C0D7)              // edge: #A1C0D7

// Money In (Income / Paid)
val MoneyInGreen = Color(0xFF2E8B57)         // --money-in: solid green #2E8B57
val MoneyInDarkEdge = Color(0xFF1F6B40)      // dark edge: #1F6B40
val MoneyInLightTint = Color(0xFFE3F4EA)     // light tint: #E3F4EA

// Money Due (Expense / Baki)
val MoneyDueRed = Color(0xFFD64545)          // --money-due: solid red #D64545
val MoneyDueDarkEdge = Color(0xFFA83030)     // dark edge: #A83030
val MoneyDueLightTint = Color(0xFFFDE6E4)    // light tint: #FDE6E4

// Text Colors
val TextMain = Color(0xFF17302A)             // --text: #17302A (headings, numbers)
val TextSub = Color(0xFF6B6B6B)              // --text-2: #6B6B6B (subtitles)
val TextOnDark = Color(0xFFFFFFFF)           // --text-on-dark: #FFFFFF
val TextMuted = Color(0xFF8A8A8A)

// Card Borders & Raised Edges
val BorderNeutral = Color(0xFFE4DFD2)        // --border: #E4DFD2
val BorderDarkEdge = Color(0xFFD8D2C2)       // bottom edge: #D8D2C2
val InputBorder = Color(0xFFD9D3C3)

// Dark Theme Tokens
val DarkThemeBg = Color(0xFF10201A)
val DarkThemeSurface = Color(0xFF18302A)
val DarkThemeText = Color(0xFFEAF3EE)
val DarkThemeBorder = Color(0xFF234438)
val DarkThemeEdge = Color(0xFF0E1F18)

// ==========================================
// Compatibility Aliases (NO BLUE TONES ANYWHERE!)
// ==========================================
val PrimaryBlue = DeepForestGreen
val PrimaryBlueGradientStart = DeepForestGreen
val PrimaryBlueGradientEnd = ForestGreenDark
val LightBlueHighlight = MintTint
val FreshGreen = MoneyInGreen
val FreshGreenLight = MoneyInLightTint
val CoralRed = MoneyDueRed
val CoralRedLight = MoneyDueLightTint
val SunshineYellow = AmberAccent
val SunshineYellowLight = CreamYellowTint
val PurpleAccent = AmberAccent
val PurpleAccentLight = CreamYellowTint
val TealAccent = MoneyInGreen
val TealAccentLight = MintTint
val PinkAccent = CoralRed
val PinkAccentLight = BlushPinkTint
val PageBackground = WarmCreamBg
val BorderColor = BorderNeutral
val BorderSlate = BorderNeutral
val BorderGray = InputBorder
val TextNavyDark = TextMain
val TextNavySecondary = TextSub
val TextPrimary = TextMain
val TextSecondary = TextSub
val ForestGreen = DeepForestGreen
val ForestGreenLight = MintTint
val AmberGold = AmberAccent
val AmberLight = CreamYellowTint
val SoftSlateBackground = WarmCreamBg
val CreamBackground = WarmCreamBg
val EmeraldKamai = MoneyInGreen
val EmeraldKamaiLight = MoneyInLightTint
val RoseKharcha = MoneyDueRed
val RoseKharchaLight = MoneyDueLightTint
val WarmAmberDue = AmberAccent
val WarmAmberDueLight = CreamYellowTint
val DueRed = MoneyDueRed
val DueRedLight = MoneyDueLightTint
val PaidGreen = MoneyInGreen
val PaidGreenLight = MoneyInLightTint
val CharcoalText = TextMain
val RedDue = MoneyDueRed
