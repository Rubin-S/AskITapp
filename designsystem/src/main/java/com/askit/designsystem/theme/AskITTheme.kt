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
private val AskITGreenLight = Color(0xFF3D7100)
private val AskITOnGreenLight = Color(0xFFFFFFFF)
private val AskITGreenContainerLight = Color(0xFFE0F6BF)
private val AskITOnGreenContainerLight = Color(0xFF1A2E00)
private val AskITGreenDark = Color(0xFF7CE605)
private val AskITOnGreenDark = Color(0xFF000000)
private val AskITGreenContainerDark = Color(0xFF315F00)
private val AskITOnGreenContainerDark = Color(0xFFD5F5A1)
private val OfflineContainerLight = Color(0xFFFFF4CC)
private val OfflineContentLight = Color(0xFF5B4300)
private val OfflineIconLight = Color(0xFF8A6400)
private val OfflineContainerDark = Color(0xFF3A2F00)
private val OfflineContentDark = Color(0xFFFFE08A)
private val OfflineIconDark = Color(0xFFFFCC4D)
private val ServerContainerLight = Color(0xFFFDECEC)
private val ServerContentLight = Color(0xFF6E1B1B)
private val ServerIconLight = Color(0xFFB3261E)
private val ServerContainerDark = Color(0xFF4A1717)
private val ServerContentDark = Color(0xFFFFDAD6)
private val ServerIconDark = Color(0xFFFFB4AB)

private val Gray850 = Color(0xFF1C1C1C)
private val Gray800 = Color(0xFF2C2C2C)
private val Gray950 = Color(0xFF121212)

private val LightScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Gray200,
    onPrimaryContainer = Black,
    secondary = AskITGreenLight,
    onSecondary = AskITOnGreenLight,
    secondaryContainer = AskITGreenContainerLight,
    onSecondaryContainer = AskITOnGreenContainerLight,
    tertiary = OfflineIconLight,
    onTertiary = White,
    tertiaryContainer = OfflineContainerLight,
    onTertiaryContainer = OfflineContentLight,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    surfaceContainerLowest = White,
    surfaceContainerLow = Gray100,
    surfaceContainer = White,
    surfaceContainerHigh = Gray100,
    surfaceContainerHighest = Gray200,
    outline = Gray600,
    outlineVariant = Gray200,
    inverseSurface = Black,
    inverseOnSurface = White,
    error = ServerIconLight,
    onError = White,
    errorContainer = ServerContainerLight,
    onErrorContainer = ServerContentLight,
    scrim = Black,
)

private val DarkScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Gray900,
    onPrimaryContainer = White,
    secondary = AskITGreenDark,
    onSecondary = AskITOnGreenDark,
    secondaryContainer = AskITGreenContainerDark,
    onSecondaryContainer = AskITOnGreenContainerDark,
    tertiary = OfflineIconDark,
    onTertiary = Black,
    tertiaryContainer = OfflineContainerDark,
    onTertiaryContainer = OfflineContentDark,
    background = Black,
    onBackground = White,
    surface = Gray950,
    onSurface = White,
    surfaceVariant = Gray900,
    onSurfaceVariant = Gray400,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Gray950,
    surfaceContainer = Gray850,
    surfaceContainerHigh = Gray900,
    surfaceContainerHighest = Gray800,
    outline = Gray600,
    outlineVariant = Gray700,
    inverseSurface = White,
    inverseOnSurface = Black,
    error = ServerIconDark,
    onError = Black,
    errorContainer = ServerContainerDark,
    onErrorContainer = ServerContentDark,
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
