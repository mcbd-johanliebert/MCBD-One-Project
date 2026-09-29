package com.mcbdone.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = TextLight,
    primaryContainer = EmeraldGlow.copy(alpha = 0.2f),
    onPrimaryContainer = EmeraldDark,
    secondary = DiamondCyan,
    onSecondary = TextLight,
    background = BgCanvasMid,
    onBackground = TextPrimary,
    surface = GlassCardBg,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceHeavy,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MCBDOneTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgCanvasTop.toArgb()
            window.navigationBarColor = BgCanvasBottom.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
