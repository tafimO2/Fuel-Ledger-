package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FuelGreenDarkPrimary,
    onPrimary = FuelGreenDarkOnPrimary,
    primaryContainer = FuelGreenDarkContainer,
    onPrimaryContainer = FuelGreenDarkOnContainer,
    secondary = PaidGreenDark,
    onSecondary = Color.Black,
    secondaryContainer = PaidGreenDarkContainer,
    onSecondaryContainer = PaidGreenDarkOnContainer,
    tertiary = OctaneColor,
    background = SurfaceDark,
    surface = CardDark,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = DueRedDark,
    errorContainer = DueRedDarkContainer,
    onErrorContainer = DueRedDarkOnContainer
)

private val LightColorScheme = lightColorScheme(
    primary = FuelGreenPrimary,
    onPrimary = FuelGreenOnPrimary,
    primaryContainer = FuelGreenContainer,
    onPrimaryContainer = FuelGreenOnContainer,
    secondary = PaidGreen,
    onSecondary = Color.White,
    secondaryContainer = PaidGreenContainer,
    onSecondaryContainer = PaidGreenOnContainer,
    tertiary = OctaneColor,
    background = SurfaceLight,
    surface = CardLight,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextMuted,
    error = DueRed,
    errorContainer = DueRedContainer,
    onErrorContainer = DueRedOnContainer
)

@Composable
fun FuelLedgerTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
