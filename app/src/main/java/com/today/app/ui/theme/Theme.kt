package com.today.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Navy900 = Color(0xFF070F2B)
private val Indigo700 = Color(0xFF1B1A55)
private val Slate500 = Color(0xFF535C91)
private val Lavender300 = Color(0xFF9290C3)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9B7E6),
    onPrimary = Color(0xFF23214E),
    primaryContainer = Color(0xFF3A3974),
    onPrimaryContainer = Color(0xFFE4E3FB),
    secondary = Lavender300,
    onSecondary = Color(0xFF1D1B45),
    secondaryContainer = Color(0xFF343266),
    onSecondaryContainer = Color(0xFFE0DFF7),
    tertiary = Color(0xFF9BA3DC),
    onTertiary = Color(0xFF101A47),
    background = Navy900,
    onBackground = Color(0xFFE7E7F4),
    surface = Navy900,
    onSurface = Color(0xFFE7E7F4),
    surfaceVariant = Color(0xFF262E58),
    onSurfaceVariant = Color(0xFFAEB3D2),
    surfaceContainerLowest = Color(0xFF050B20),
    surfaceContainerLow = Color(0xFF0C1330),
    surfaceContainer = Color(0xFF11183A),
    surfaceContainerHigh = Color(0xFF171F46),
    surfaceContainerHighest = Color(0xFF1E2752),
    surfaceBright = Color(0xFF2A3263),
    surfaceDim = Color(0xFF070F2B),
    outline = Color(0xFF3C4470),
    outlineVariant = Color(0xFF272F58),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE7E7F4),
    inverseOnSurface = Color(0xFF1B1B27),
    scrim = Color(0xFF000000)
)

private val LightColors = lightColorScheme(
    primary = Indigo700,
    onPrimary = Color.White,
    primaryContainer = Lavender300,
    onPrimaryContainer = Navy900,
    secondary = Slate500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0DFF3),
    onSecondaryContainer = Color(0xFF1B1A55),
    tertiary = Slate500,
    onTertiary = Color.White,
    background = Color(0xFFF7F7FC),
    onBackground = Navy900,
    surface = Color(0xFFF7F7FC),
    onSurface = Navy900,
    surfaceVariant = Color(0xFFE3E2F0),
    onSurfaceVariant = Color(0xFF46464F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F1F9),
    surfaceContainer = Color(0xFFECEBF5),
    surfaceContainerHigh = Color(0xFFE6E5F0),
    surfaceContainerHighest = Color(0xFFE0DFEB),
    outline = Color(0xFF777680),
    outlineVariant = Color(0xFFC7C5D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun TodayTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TodayTypography,
        shapes = TodayShapes,
        content = content
    )
}
