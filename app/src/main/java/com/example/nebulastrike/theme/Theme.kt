package com.example.nebulastrike.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaceColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),     // Cyber Cyan
    secondary = Color(0xFFD500F9),   // Neon Magenta
    tertiary = Color(0xFF00E676),    // Emerald Green
    background = Color(0xFF060A17),  // Deep Outer Space
    surface = Color(0xFF0F172A),     // Dark Slate
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun NebulaStrikeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpaceColorScheme,
        typography = Typography,
        content = content
    )
}
