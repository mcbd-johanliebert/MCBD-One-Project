package com.mcbdone.app.ui.theme

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * World-class Glassmorphic background mesh with luminous emerald and diamond orbs.
 * When placed behind glass cards, this creates realistic light refraction and depth.
 */
@Composable
fun AmbientGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_ambient")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F5F9),
                        Color(0xFFF8FAFC),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        // Glowing Aurora Emerald Orb Top-Left
        Box(
            modifier = Modifier
                .size((320 * pulse1).dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3810B981), // Frosted Emerald
                            Color(0x1534D399),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        // Glowing Diamond Prismarine Orb Top-Right
        Box(
            modifier = Modifier
                .size((280 * (2f - pulse1)).dp)
                .offset(x = 180.dp, y = 40.dp)
                .blur(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3006B6D4), // Cyan Diamond
                            Color(0x120284C7),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        // Warm Golden Glowstone Orb Bottom-Right
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 100.dp, y = 500.dp)
                .blur(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x18F59E0B),
                            Color(0x0AFBBF24),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        // Subtle Minecraft Grid Overlay for authentic community feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x06000000)
                        ),
                        radius = 1200f
                    )
                )
        )

        content()
    }
}

/**
 * Reusable Glassmorphism Surface Composable
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassCardBg,
    borderStroke: Brush = GlassBorderGradient,
    borderWidth: Dp = 1.2.dp,
    elevation: Dp = 10.dp,
    shadowColor: Color = Color(0x140F172A),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderStroke, shape)
    ) {
        // Specular Top Sheen Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x40FFFFFF),
                            Color(0x00FFFFFF)
                        )
                    )
                )
        )

        content()
    }
}
