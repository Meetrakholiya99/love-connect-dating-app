package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF561334),
    onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = OrchidPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF331B57),
    onSecondaryContainer = Color(0xFFEBD9FF),
    tertiary = ChampagneGold,
    onTertiary = Color(0xFF281800),
    tertiaryContainer = Color(0xFF473108),
    onTertiaryContainer = Color(0xFFFFE7B8),
    background = VelvetObsidian,
    onBackground = VelvetTextPrimary,
    surface = VelvetSurface,
    onSurface = VelvetTextPrimary,
    surfaceVariant = VelvetSurfaceElevated,
    onSurfaceVariant = VelvetTextSecondary,
    outline = VelvetSurfaceBorder,
    error = CoralAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RoseDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E5),
    onPrimaryContainer = Color(0xFF4A001E),
    secondary = OrchidDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0DBFF),
    onSecondaryContainer = Color(0xFF2D004E),
    tertiary = Color(0xFFB57B14),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBC2),
    onTertiaryContainer = Color(0xFF3B2600),
    background = SilkIvory,
    onBackground = SilkTextPrimary,
    surface = SilkSurface,
    onSurface = SilkTextPrimary,
    surfaceVariant = SilkSurfaceVariant,
    onSurfaceVariant = SilkTextSecondary,
    outline = SilkBorder,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

val LoveConnectShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

object LoveConnectGradients {
    val BrandPrimary = Brush.linearGradient(
        colors = listOf(RosePrimary, OrchidPurple)
    )
    val PremiumGold = Brush.linearGradient(
        colors = listOf(ChampagneGold, RosePrimary)
    )
    val DarkAtmosphere = Brush.verticalGradient(
        colors = listOf(Color(0xFF1D0B30), VelvetObsidian)
    )
    val CardScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0x440B0512),
            Color(0xE60B0512)
        )
    )
}

@Composable
fun LoveConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LoveConnectTypography,
        shapes = LoveConnectShapes,
        content = content
    )
}
