package com.mcbdone.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
 * World-class Awwwards-inspired Glassmorphic background mesh.
 * Dynamically adapts to any screen aspect ratio (Phones, Foldables, Tablets).
 * Features smooth, breathing chromatic aurora orbs that drift gently
 * creating authentic physical optical refraction behind frosted glass surfaces.
 */
@Composable
fun AmbientGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_ambient")

    // Breathing pulse for orbs
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Gentle lateral drift
    val driftX by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )

    val driftY by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F5F9), // Pure pristine slate canvas
                        Color(0xFFF8FAFC),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // Dynamic Glowing Aurora Emerald Orb Top-Left
        Box(
            modifier = Modifier
                .size((screenW * 0.75f * pulse).coerceIn(280.dp, 520.dp))
                .offset(
                    x = (-screenW * 0.15f) + driftX.dp,
                    y = (-screenH * 0.08f) + driftY.dp
                )
                .blur(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3810B981), // Emerald glow
                            Color(0x1834D399),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Dynamic Glowing Cyan Diamond Prismarine Orb Top-Right
        Box(
            modifier = Modifier
                .size((screenW * 0.7f * (2f - pulse)).coerceIn(260.dp, 480.dp))
                .offset(
                    x = (screenW * 0.55f) - driftX.dp,
                    y = (screenH * 0.05f) - driftY.dp
                )
                .blur(85.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3206B6D4), // Cyan Diamond glow
                            Color(0x150284C7),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Dynamic Warm Golden Glowstone Orb Middle/Bottom
        Box(
            modifier = Modifier
                .size((screenW * 0.65f * pulse).coerceIn(240.dp, 460.dp))
                .offset(
                    x = (screenW * 0.2f) + driftY.dp,
                    y = (screenH * 0.55f) + driftX.dp
                )
                .blur(95.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1AF59E0B), // Warm Glowstone amber
                            Color(0x0CFBBF24),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Subtle Refractive Micro-Grid Texture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x05000000)
                        ),
                        radius = 1400f
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
        // Specular Top Sheen Highlight (Simulates ambient glass reflection)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x4DFFFFFF),
                            Color(0x00FFFFFF)
                        )
                    )
                )
        )

        content()
    }
}

/**
 * Shimmer Sweep Modifier for featured hero cards and promotional banners
 */
@Composable
fun Modifier.glassShimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_trans"
    )

    return this.background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.25f),
                Color.Transparent
            ),
            start = Offset(translateAnim, 0f),
            end = Offset(translateAnim + 250f, 250f)
        )
    )
}
