package com.example.travelcompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = OnPrimaryLight,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = TealSecondary,
    onSecondary = OnSecondaryLight,
    secondaryContainer = TealContainer,
    onSecondaryContainer = OnTealContainer,
    tertiary = GoldTertiary,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = OnGoldContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val DarkColors = darkColorScheme(
    primary = TerracottaPrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = OnTerracottaContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = TealContainerDark,
    onSecondaryContainer = OnTealContainerDark,
    tertiary = GoldTertiaryDark,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = OnGoldContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun TravelCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Фиксированная фирменная палитра — без dynamicColor, чтобы стиль был единым на всех устройствах.
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
