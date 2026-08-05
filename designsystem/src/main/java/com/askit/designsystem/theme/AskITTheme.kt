package com.askit.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)
private val Gray900 = Color(0xFF212121)
private val Gray700 = Color(0xFF616161)
private val Gray600 = Color(0xFF8A8A8A)
private val Gray400 = Color(0xFFBDBDBD)
private val Gray200 = Color(0xFFEEEEEE)
private val Gray100 = Color(0xFFF5F5F5)

private val LightScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Gray200,
    onPrimaryContainer = Black,
    secondary = Gray900,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Black,
    tertiary = Gray700,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    surfaceContainer = White,
    surfaceContainerHigh = Gray100,
    outline = Gray600,
    outlineVariant = Gray200,
    inverseSurface = Black,
    inverseOnSurface = White,
    error = Black,
    onError = White,
    scrim = Black,
)

private val DarkScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Gray900,
    onPrimaryContainer = White,
    secondary = Gray200,
    onSecondary = Black,
    secondaryContainer = Gray900,
    onSecondaryContainer = White,
    tertiary = Gray400,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = Gray900,
    onSurfaceVariant = Gray400,
    surfaceContainer = Black,
    surfaceContainerHigh = Gray900,
    outline = Gray700,
    outlineVariant = Gray900,
    inverseSurface = White,
    inverseOnSurface = Black,
    error = White,
    onError = Black,
    scrim = Black,
)

@Composable
fun AskITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
