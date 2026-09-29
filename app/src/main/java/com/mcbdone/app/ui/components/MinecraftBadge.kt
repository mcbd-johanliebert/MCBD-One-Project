package com.mcbdone.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcbdone.app.ui.theme.*

@Composable
fun MinecraftRankBadge(
    rank: String,
    modifier: Modifier = Modifier
) {
    val cleanRank = rank
        .replace("👑", "")
        .replace("🛡️", "")
        .replace("💎", "")
        .replace("⚡", "")
        .replace("⚔️", "")
        .replace("⛏️", "")
        .trim()
        .ifEmpty { "Survivalist" }

    val (bgGradient, borderColor, textColor, rankIcon) = when {
        cleanRank.contains("Admin", ignoreCase = true) -> Quadruple(
            Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))),
            Color(0xFFF59E0B),
            Color(0xFF92400E),
            Icons.Default.AdminPanelSettings
        )
        cleanRank.contains("Mod", ignoreCase = true) -> Quadruple(
            Brush.linearGradient(listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))),
            Color(0xFF8B5CF6),
            Color(0xFF5B21B6),
            Icons.Default.Shield
        )
        cleanRank.contains("Diamond", ignoreCase = true) -> Quadruple(
            Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))),
            Color(0xFF0284C7),
            Color(0xFF0369A1),
            Icons.Default.Diamond
        )
        cleanRank.contains("Redston", ignoreCase = true) -> Quadruple(
            Brush.linearGradient(listOf(Color(0xFFFEE2E2), Color(0xFFFECACA))),
            Color(0xFFEF4444),
            Color(0xFF991B1B),
            Icons.Default.Bolt
        )
        cleanRank.contains("PvP", ignoreCase = true) -> Quadruple(
            Brush.linearGradient(listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))),
            Color(0xFFEC4899),
            Color(0xFF9D174D),
            Icons.Default.MilitaryTech
        )
        else -> Quadruple(
            Brush.linearGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))),
            Color(0xFF10B981),
            Color(0xFF065F46),
            Icons.Default.Shield
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgGradient)
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = rankIcon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = cleanRank,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
