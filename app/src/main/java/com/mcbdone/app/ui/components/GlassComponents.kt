package com.mcbdone.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcbdone.app.data.model.ChatMessage
import com.mcbdone.app.ui.theme.*

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "btn_scale")

    val bgBrush = if (isPrimary) {
        Brush.linearGradient(
            listOf(
                EmeraldPrimary,
                Color(0xFF059669)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xE6FFFFFF),
                Color(0xCCF8FAFC)
            )
        )
    }

    val textColor = if (isPrimary) Color.White else TextPrimary
    val strokeBrush = if (isPrimary) {
        Brush.linearGradient(listOf(Color(0x80FFFFFF), Color(0x3010B981)))
    } else GlassBorderGradient

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPrimary) 12.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isPrimary) EmeraldPrimary.copy(alpha = 0.35f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .border(1.2.dp, strokeBrush, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = textColor,
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    tint: Color = TextPrimary,
    badgeCount: Int = 0
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape, spotColor = Color(0x15000000))
            .clip(CircleShape)
            .background(Color(0xD9FFFFFF))
            .border(1.dp, GlassBorderGradient, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(RedstoneAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type a message...",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xE0FFFFFF))
            .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Composable
fun GlassTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = Color(0x12000000))
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xDDFFFFFF))
            .border(1.dp, GlassBorderGradient, RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Minecraft Bangladesh Icon Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2010B981))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "🇧🇩 MCBD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    }
                }
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}

@Composable
fun GlassBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("chats", "Chats", Icons.Default.ChatBubbleOutline),
        Triple("servers", "Servers", Icons.Default.Dns),
        Triple("showcase", "Showcase", Icons.Default.Diamond),
        Triple("profile", "Profile", Icons.Default.PersonOutline)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0x18000000))
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xEBFFFFFF))
            .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(32.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, label, icon) ->
                val selected = currentRoute == route
                val bgModifier = if (selected) {
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x2210B981))
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                } else {
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigate(route) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                }

                Row(
                    modifier = bgModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) EmeraldDark else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = EmeraldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val bubbleBg = if (isCurrentUser) {
        Brush.linearGradient(
            listOf(
                Color(0xFF10B981),
                Color(0xFF059669)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xF2FFFFFF),
                Color(0xE6F8FAFC)
            )
        )
    }

    val bubbleBorder = if (isCurrentUser) {
        Brush.linearGradient(listOf(Color(0x60FFFFFF), Color(0x3010B981)))
    } else GlassBorderGradient

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            MinecraftAvatar(
                avatarUrl = message.userAvatar,
                ign = message.minecraftIgn,
                size = 36.dp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Header for incoming messages
            if (!isCurrentUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                ) {
                    Text(
                        text = message.userName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    MinecraftRankBadge(rank = message.userRank)
                }
            }

            // Glass Bubble Box
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = if (isCurrentUser) 6.dp else 3.dp,
                        shape = bubbleShape,
                        spotColor = if (isCurrentUser) EmeraldPrimary.copy(alpha = 0.25f) else Color(0x10000000)
                    )
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .border(1.dp, bubbleBorder, bubbleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    // Reply Preview
                    if (!message.replyToContent.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrentUser) Color(0x25000000) else Color(0x10000000))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${message.replyToSender ?: "Reply"}: ${message.replyToContent}",
                                fontSize = 11.sp,
                                color = if (isCurrentUser) Color(0xCCFFFFFF) else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = if (isCurrentUser) Color.White else TextPrimary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.createdAt,
                        fontSize = 10.sp,
                        color = if (isCurrentUser) Color(0xB3FFFFFF) else TextMuted,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Reactions Bar
            if (message.reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.reactions.forEach { (emoji, count) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xD9FFFFFF))
                                .border(1.dp, GlassBorderGradient, RoundedCornerShape(12.dp))
                                .clickable { onReactionClick(emoji) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$emoji $count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
