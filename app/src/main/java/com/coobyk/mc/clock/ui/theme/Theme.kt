package com.coobyk.mc.clock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = McGreen,
    onPrimary = AppText,
    secondary = McGreenDark,
    background = AppBg,
    onBackground = AppText,
    surface = AppSurface,
    onSurface = AppText,
    surfaceVariant = AppSurfaceLight,
    onSurfaceVariant = AppTextSecondary,
    outline = AppBorder,
)

@Composable
fun McTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = McTypography,
        content = content,
    )
}