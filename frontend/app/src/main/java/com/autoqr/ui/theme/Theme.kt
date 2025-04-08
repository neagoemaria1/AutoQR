package com.autoqr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Blue40,
    secondary = Green40,
    tertiary = Teal40,
    background = DarkGray,
    surface = Black,
    onPrimary = White,
    onSecondary = White,
    onBackground = White,
    onSurface = White
)

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Blue80,
    secondary = Green80,
    tertiary = Teal80,
    background = LightGray,
    surface = White,
    onPrimary = DarkGray,
    onSecondary = DarkGray,
    onBackground = DarkGray,
    onSurface = DarkGray
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
