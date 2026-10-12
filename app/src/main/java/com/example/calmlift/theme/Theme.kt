package com.example.calmlift.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * CalmLift Dark-Mode First Color Scheme.
 * Built around deep dark surfaces and restorative sage & amber accents
 * to lower cognitive stress and sensory overload in busy gym environments.
 */
private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = SageGreenPrimary,
    onPrimary = SageGreenOnPrimary,
    primaryContainer = SageGreenContainer,
    onPrimaryContainer = SageGreenOnContainer,

    secondary = WarmAmberSecondary,
    onSecondary = WarmAmberOnSecondary,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = WarmAmberOnContainer,

    tertiary = SymmetryBlueTertiary,
    onTertiary = SymmetryBlueOnTertiary,
    tertiaryContainer = SymmetryBlueContainer,
    onTertiaryContainer = SymmetryBlueOnContainer,

    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = OutlineMuted
)

@Composable
fun CalmLiftTheme(
    content: @Composable () -> Unit
) {
    // We enforce the deep dark mode first theme to prevent jarring bright screens in the gym
    MaterialTheme(
        colorScheme = ExpressiveDarkColorScheme,
        shapes = CalmLiftShapes,
        typography = Typography,
        content = content
    )
}
