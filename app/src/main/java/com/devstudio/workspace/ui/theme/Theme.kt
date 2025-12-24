package com.devstudio.workspace.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    background = ForestBackground,
    onBackground = ForestOnBackground,
    surface = ForestSurface,
    onSurface = ForestOnSurface
)

private val OceanColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OceanOnPrimaryContainer,
    background = OceanBackground,
    onBackground = OceanOnBackground,
    surface = OceanSurface,
    onSurface = OceanOnSurface
)

private val SunsetColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = SunsetOnPrimary,
    primaryContainer = SunsetPrimaryContainer,
    onPrimaryContainer = SunsetOnPrimaryContainer,
    background = SunsetBackground,
    onBackground = SunsetOnBackground,
    surface = SunsetSurface,
    onSurface = SunsetOnSurface
)

@Composable
fun WorkspaceTheme(
    theme: AppTheme = AppTheme.FOREST,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.OCEAN -> OceanColorScheme
        AppTheme.SUNSET -> SunsetColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
