package com.nutriagricola.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = White,
    secondary = LeafGreen,
    onSecondary = White,
    primaryContainer = SurfaceGreenSoft,
    onPrimaryContainer = ForestGreen,
    background = BackgroundCream,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGreenSoft,
    onSurfaceVariant = TextSecondary,
    outline = BorderSoft,
    surfaceTint = ForestGreen
)

@Composable
fun NutriAgricolaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
