package com.himanshu.ecoscope.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🌙 Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FFAA),         // Neon green
    onPrimary = Color.Black,
    secondary = Color(0xFF80D8FF),       // Soft cyan
    onSecondary = Color.Black,
    background = Color(0xFF0B0B0B),      // Deep dark
    onBackground = Color.White,
    surface = Color(0xFF1C1C1C),         // Dark surface for cards
    onSurface = Color.White
)

// ☀️ Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007F5F),
    onPrimary = Color.White,
    secondary = Color(0xFF00B4D8),
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color.Black,
    surface = Color(0xFFF2F2F2),
    onSurface = Color.Black
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
