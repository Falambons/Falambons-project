package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemeMode

private val PureDarkColorScheme = darkColorScheme(
    primary = PureDarkAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = PureDarkAccent,
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color.Black,
    background = PureDarkBackground,
    onBackground = Color(0xFFEDEDED),
    surface = PureDarkSurface,
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = PureDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = PureDarkBorder,
    outlineVariant = Color(0xFF222226)
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = BlueDarkAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F264A),
    onPrimaryContainer = BlueDarkAccent,
    secondary = Color(0xFF60A5FA),
    onSecondary = Color.Black,
    background = BlueDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = BlueDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = BlueDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = BlueDarkBorder,
    outlineVariant = Color(0xFF14244E)
)

private val RgbCyberColorScheme = darkColorScheme(
    primary = RgbNeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2D123D),
    onPrimaryContainer = RgbNeonPink,
    secondary = RgbNeonPink,
    onSecondary = Color.White,
    tertiary = RgbNeonGreen,
    background = RgbBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = RgbSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = RgbSurfaceVariant,
    onSurfaceVariant = Color(0xFFC084FC),
    outline = RgbBorder,
    outlineVariant = Color(0xFF3B1E54)
)

@Composable
fun FalambonsCodeTheme(
    themeMode: AppThemeMode = AppThemeMode.PURE_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.PURE_DARK -> PureDarkColorScheme
        AppThemeMode.BLUE_DARK -> BlueDarkColorScheme
        AppThemeMode.RGB -> RgbCyberColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
