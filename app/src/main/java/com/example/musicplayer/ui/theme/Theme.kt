package com.example.musicplayer.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFB39DDB),
    secondary = Color(0xFFCE93D8),
    tertiary = Color(0xFFF48FB1),
    surface = Color(0xFF1A1A2E),
    background = Color.Transparent
)

@Composable
fun MusicPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
