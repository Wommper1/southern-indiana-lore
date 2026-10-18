package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LoreDarkColorScheme = darkColorScheme(
    primary = LanternAmberBright,
    onPrimary = OnLanternAmber,
    primaryContainer = LanternAmberContainer,
    onPrimaryContainer = LanternAmberBright,
    secondary = ParchmentPaper,
    onSecondary = SlateNight,
    secondaryContainer = SlateNightSurface,
    onSecondaryContainer = ParchmentPaper,
    tertiary = KnobstoneForest,
    onTertiary = Color.White,
    background = SlateNight,
    onBackground = ParchmentPaper,
    surface = SlateNightElevated,
    onSurface = ParchmentPaper,
    surfaceVariant = SlateNightSurface,
    onSurfaceVariant = ParchmentMuted,
    outline = SlateBorder,
    error = GhostCrimson,
    onError = Color.White
)

private val LoreLightColorScheme = lightColorScheme(
    primary = BronzeDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = BronzeDark,
    secondary = Color(0xFF451A03),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1E4D0),
    onSecondaryContainer = Color(0xFF271307),
    tertiary = KnobstoneForest,
    onTertiary = Color.White,
    background = ParchmentLight,
    onBackground = Color(0xFF1E293B),
    surface = ParchmentLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1E9DA),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = GhostCrimson,
    onError = Color.White
)

@Composable
fun SouthernIndianaLoreTheme(
    darkTheme: Boolean = true, // Default to immersive dark mode for folklore/night tour atmosphere & AMOLED S25 Ultra
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LoreDarkColorScheme else LoreLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
