package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Pure Dark Palette
val PureDarkBackground = Color(0xFF000000)
val PureDarkSurface = Color(0xFF101012)
val PureDarkSurfaceVariant = Color(0xFF18181C)
val PureDarkBorder = Color(0xFF26262B)
val PureDarkAccent = Color(0xFF38BDF8)

// Blue Dark Palette
val BlueDarkBackground = Color(0xFF050B18)
val BlueDarkSurface = Color(0xFF0C162E)
val BlueDarkSurfaceVariant = Color(0xFF132247)
val BlueDarkBorder = Color(0xFF1E356A)
val BlueDarkAccent = Color(0xFF00E5FF)

// RGB Cyber Theme Palette
val RgbBackground = Color(0xFF08080E)
val RgbSurface = Color(0xFF10101A)
val RgbSurfaceVariant = Color(0xFF181826)
val RgbBorder = Color(0xFF2E1A47)
val RgbNeonPink = Color(0xFFFF007F)
val RgbNeonCyan = Color(0xFF00F0FF)
val RgbNeonGreen = Color(0xFF00FF66)
val RgbNeonYellow = Color(0xFFFFE600)
val RgbNeonPurple = Color(0xFFA855F7)

// Dynamic RGB gradient brush for glowing borders and headers
val RgbGradient = Brush.horizontalGradient(
    colors = listOf(
        RgbNeonPink,
        RgbNeonPurple,
        RgbNeonCyan,
        RgbNeonGreen
    )
)

val RgbGlowBrush = Brush.sweepGradient(
    colors = listOf(
        RgbNeonPink,
        RgbNeonCyan,
        RgbNeonGreen,
        RgbNeonYellow,
        RgbNeonPink
    )
)

// Syntax Highlighting Colors
val SyntaxKeyword = Color(0xFFFF7B72)
val SyntaxFunction = Color(0xFFD2A8FF)
val SyntaxString = Color(0xFFA5D6FF)
val SyntaxNumber = Color(0xFF79C0FF)
val SyntaxComment = Color(0xFF8B949E)
val SyntaxTag = Color(0xFF7EE787)
val SyntaxVariable = Color(0xFFFFA657)
val SyntaxBracket = Color(0xFFF0883E)
val LineNumberColor = Color(0xFF484F58)
