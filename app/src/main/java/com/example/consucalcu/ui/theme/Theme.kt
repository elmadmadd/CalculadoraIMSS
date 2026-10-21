package com.example.consucalcu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.consucalcu.*

private val DarkColorScheme = darkColorScheme(
    primary = ConsuOrange, // Brand Orange as primary accent in Dark
    onPrimary = Color.White,
    primaryContainer = ConsuNavyLight,
    onPrimaryContainer = Color.White,
    secondary = ConsuOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = ConsuSurfaceVarDark,
    onSecondaryContainer = ConsuTextLight,
    tertiary = Color.White,
    onTertiary = Color.Black,
    background = ConsuBgDark,
    onBackground = ConsuTextLight,
    surface = ConsuSurfaceDark,
    onSurface = ConsuTextLight,
    surfaceVariant = ConsuSurfaceVarDark,
    onSurfaceVariant = ConsuTextLight.copy(alpha = 0.7f),
    outline = ConsuDividerDark,
    error = ConsuError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ConsuNavy, // Professional Navy as primary in Light
    onPrimary = Color.White,
    primaryContainer = ConsuChipBg,
    onPrimaryContainer = ConsuTextPrimary,
    secondary = ConsuOrange,
    onSecondary = Color.White,
    secondaryContainer = ConsuSurfaceVarLight,
    onSecondaryContainer = ConsuTextPrimary,
    tertiary = ConsuOrangeDark,
    onTertiary = Color.White,
    background = ConsuBgLight,
    onBackground = ConsuTextPrimary,
    surface = ConsuSurfaceLight,
    onSurface = ConsuTextPrimary,
    surfaceVariant = ConsuSurfaceVarLight,
    onSurfaceVariant = ConsuTextPrimary.copy(alpha = 0.7f),
    outline = ConsuDividerLight,
    error = ConsuError,
    onError = Color.White
)

@Composable
fun ConsucalcuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ConsuTypography,
        content = content
    )
}
