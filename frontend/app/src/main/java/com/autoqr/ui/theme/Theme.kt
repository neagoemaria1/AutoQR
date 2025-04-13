package com.autoqr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.autoqr.ui.Color.*

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Black,
    secondary = LimeGreen,
    onSecondary = Black,
    tertiary = VioletPurple,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    error = ErrorRed,
    onError = Black
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = White,
    secondary = LimeGreen,
    onSecondary = Black,
    tertiary = VioletPurple,
    onTertiary = White,
    background = LightGray,
    onBackground = DarkGray,
    surface = White,
    onSurface = DarkGray,
    error = ErrorRed,
    onError = White
)

@Composable
fun AutoQRTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
