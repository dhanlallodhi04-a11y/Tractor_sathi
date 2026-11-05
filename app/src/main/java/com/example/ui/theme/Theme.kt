package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KisanSaathiLightColorScheme = lightColorScheme(
    primary = DeepForestGreen,
    onPrimary = Color.White,
    primaryContainer = MintTint,
    onPrimaryContainer = ForestGreenDark,
    secondary = AmberAccent,
    onSecondary = Color.White,
    secondaryContainer = CreamYellowTint,
    onSecondaryContainer = AmberDark,
    background = WarmCreamBg,
    onBackground = TextMain,
    surface = CardWhite,
    onSurface = TextMain,
    surfaceVariant = MintTint,
    onSurfaceVariant = TextSub,
    outline = BorderNeutral,
    outlineVariant = BorderDarkEdge,
    error = MoneyDueRed,
    onError = Color.White,
    errorContainer = MoneyDueLightTint,
    onErrorContainer = MoneyDueDarkEdge
)

private val KisanSaathiDarkColorScheme = darkColorScheme(
    primary = DeepForestGreen,
    onPrimary = Color.White,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = MintTint,
    secondary = AmberAccent,
    onSecondary = Color.White,
    secondaryContainer = AmberDark,
    onSecondaryContainer = CreamYellowTint,
    background = DarkThemeBg,
    onBackground = DarkThemeText,
    surface = DarkThemeSurface,
    onSurface = DarkThemeText,
    surfaceVariant = DarkThemeSurface,
    onSurfaceVariant = Color(0xFFA5B8AD),
    outline = DarkThemeBorder,
    outlineVariant = DarkThemeEdge,
    error = MoneyDueRed,
    onError = Color.White,
    errorContainer = MoneyDueDarkEdge,
    onErrorContainer = MoneyDueLightTint
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KisanSaathiDarkColorScheme else KisanSaathiLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = KhetSaathiTypography,
        content = content
    )
}
