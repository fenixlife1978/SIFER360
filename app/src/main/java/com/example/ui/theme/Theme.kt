package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val A2SoftwayColorScheme = lightColorScheme(
    primary = A2TitleBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7D9E8),
    onPrimaryContainer = A2TitleNavyDark,
    secondary = A2TitleNavyDark,
    onSecondary = Color.White,
    secondaryContainer = A2ControlFace,
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = A2GoldRate,
    onTertiary = Color.White,
    background = A2WindowBg,
    onBackground = Color(0xFF1E293B),
    surface = A2SurfaceWhite,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = A2ControlFace,
    onSurfaceVariant = Color(0xFF475569),
    outline = A2BorderMid,
    outlineVariant = A2BorderLight
)

@Composable
fun ErpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = A2SoftwayColorScheme,
        typography = Typography,
        content = content
    )
}
