package com.mcbdone.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mcbdone.app.ui.theme.EmeraldPrimary
import com.mcbdone.app.ui.theme.GlassBorderGradient

@Composable
fun MinecraftAvatar(
    avatarUrl: String?,
    ign: String = "Steve",
    size: Dp = 44.dp,
    showOnlineBadge: Boolean = true,
    isOnline: Boolean = true,
    borderGlow: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val finalUrl = when {
        !avatarUrl.isNullOrEmpty() && avatarUrl.startsWith("http") -> avatarUrl
        ign.isNotEmpty() -> "https://crafthead.net/helm/$ign"
        else -> "https://crafthead.net/helm/Steve"
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer Frosted Glass Glow Ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (borderGlow) 8.dp else 3.dp,
                    shape = RoundedCornerShape(percent = 28),
                    spotColor = if (borderGlow) EmeraldPrimary.copy(alpha = glowAlpha) else Color(0x18000000)
                )
                .clip(RoundedCornerShape(percent = 28))
                .background(
                    brush = if (borderGlow) {
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF10B981),
                                Color(0xFF06B6D4),
                                Color(0xFFFFFFFF),
                                Color(0xFF10B981)
                            )
                        )
                    } else GlassBorderGradient
                )
                .padding(1.5.dp)
                .clip(RoundedCornerShape(percent = 26))
                .background(Color(0xFFE2E8F0))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(finalUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Minecraft skin of $ign",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Online Status Glass Pill
        if (showOnlineBadge) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.32f).coerceIn(10f, 15f).dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) EmeraldPrimary else Color(0xFF94A3B8))
            )
        }
    }
}
