package com.mcbdone.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// White Themed Palette
val BgCanvasTop = Color(0xFFF1F5F9)
val BgCanvasMid = Color(0xFFF8FAFC)
val BgCanvasBottom = Color(0xFFFFFFFF)

// Glass Surfaces
val GlassUltraLight = Color(0x4DFFFFFF) // 30%
val GlassLight = Color(0x80FFFFFF)      // 50%
val GlassCardBg = Color(0xB8FFFFFF)     // 72%
val GlassSurfaceHeavy = Color(0xEBFFFFFF)// 92%

// Glass Stroke Gradients
val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0x80FFFFFF),
        Color(0x3010B981),
        Color(0x60FFFFFF)
    )
)

val GlassEmeraldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF10B981),
        Color(0xFF059669)
    )
)

val GlassDiamondGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF06B6D4),
        Color(0xFF0284C7)
    )
)

val GlassGoldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF59E0B),
        Color(0xFFD97706)
    )
)

// Accent Colors (Minecraft Bangladesh themed)
val EmeraldPrimary = Color(0xFF10B981)
val EmeraldDark = Color(0xFF047857)
val EmeraldGlow = Color(0xFF6EE7B7)

val DiamondCyan = Color(0xFF06B6D4)
val DiamondGlow = Color(0xFF67E8F9)

val RedstoneAccent = Color(0xFFEF4444)
val GoldAccent = Color(0xFFF59E0B)

// Typography Colors
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)
val TextMuted = Color(0xFF94A3B8)
val TextLight = Color(0xFFFFFFFF)
