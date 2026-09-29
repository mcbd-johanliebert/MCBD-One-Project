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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RoleOption(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String,
    val primaryColor: Color
)

val ASSIGNABLE_ROLES = listOf(
    RoleOption(
        id = "Developer",
        name = "Developer",
        icon = Icons.Default.Terminal,
        description = "Root access, system developer, database storage & OTA control",
        primaryColor = Color(0xFF0284C7)
    ),
    RoleOption(
        id = "Admin",
        name = "Admin",
        icon = Icons.Default.AdminPanelSettings,
        description = "Highest server administration, badge delegation & announcements",
        primaryColor = Color(0xFFD97706)
    ),
    RoleOption(
        id = "Executive",
        name = "Executive",
        icon = Icons.Default.WorkspacePremium,
        description = "Executive board, tournament management & senior leadership",
        primaryColor = Color(0xFF7C3AED)
    ),
    RoleOption(
        id = "Senior Moderator",
        name = "Senior Moderator",
        icon = Icons.Default.Security,
        description = "Advanced community moderation, dispute resolution & inspection",
        primaryColor = Color(0xFF2563EB)
    ),
    RoleOption(
        id = "Group Moderator",
        name = "Group Moderator",
        icon = Icons.Default.Gavel,
        description = "Chat regulation, player guidance & channel enforcement",
        primaryColor = Color(0xFF059669)
    ),
    RoleOption(
        id = "Member",
        name = "Member",
        icon = Icons.Default.Person,
        description = "Standard verified community player & survivalist",
        primaryColor = Color(0xFF64748B)
    )
)

data class RankVisual(
    val bgGradient: Brush,
    val borderColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val displayName: String
)

fun getRankVisual(cleanRank: String): RankVisual {
    return when {
        cleanRank.contains("Developer", ignoreCase = true) || cleanRank.contains("Dev", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))),
            borderColor = Color(0xFF0284C7),
            textColor = Color(0xFF0369A1),
            icon = Icons.Default.Terminal,
            displayName = "Developer"
        )
        cleanRank.contains("Admin", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))),
            borderColor = Color(0xFFF59E0B),
            textColor = Color(0xFF92400E),
            icon = Icons.Default.AdminPanelSettings,
            displayName = "Admin"
        )
        cleanRank.contains("Executive", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF))),
            borderColor = Color(0xFFA855F7),
            textColor = Color(0xFF6B21A8),
            icon = Icons.Default.WorkspacePremium,
            displayName = "Executive"
        )
        cleanRank.contains("Senior Mod", ignoreCase = true) || cleanRank.contains("Sr Mod", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFDBEAFE), Color(0xFFBFDBFE))),
            borderColor = Color(0xFF3B82F6),
            textColor = Color(0xFF1E40AF),
            icon = Icons.Default.Security,
            displayName = "Senior Moderator"
        )
        cleanRank.contains("Mod", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))),
            borderColor = Color(0xFF10B981),
            textColor = Color(0xFF065F46),
            icon = Icons.Default.Gavel,
            displayName = "Group Moderator"
        )
        cleanRank.contains("Diamond", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))),
            borderColor = Color(0xFF0284C7),
            textColor = Color(0xFF0369A1),
            icon = Icons.Default.Diamond,
            displayName = "Diamond Member"
        )
        cleanRank.contains("Redston", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFFEE2E2), Color(0xFFFECACA))),
            borderColor = Color(0xFFEF4444),
            textColor = Color(0xFF991B1B),
            icon = Icons.Default.Bolt,
            displayName = "Redstone Tech"
        )
        cleanRank.contains("PvP", ignoreCase = true) -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))),
            borderColor = Color(0xFFEC4899),
            textColor = Color(0xFF9D174D),
            icon = Icons.Default.MilitaryTech,
            displayName = "PvP Champion"
        )
        else -> RankVisual(
            bgGradient = Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))),
            borderColor = Color(0xFF94A3B8),
            textColor = Color(0xFF334155),
            icon = Icons.Default.Person,
            displayName = cleanRank.ifEmpty { "Member" }
        )
    }
}

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
        .ifEmpty { "Member" }

    val visual = getRankVisual(cleanRank)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(visual.bgGradient)
            .border(1.dp, visual.borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = visual.icon,
            contentDescription = visual.displayName,
            tint = visual.textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = visual.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = visual.textColor
        )
    }
}
