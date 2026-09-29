package com.mcbdone.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcbdone.app.data.model.Channel
import com.mcbdone.app.data.repository.ChatRepository
import com.mcbdone.app.ui.components.*
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatListScreen(
    chatRepository: ChatRepository,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val channels by chatRepository.channels.collectAsState()
    val currentChannel by chatRepository.currentChannel.collectAsState()
    val currentUser by chatRepository.currentUser.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAdminCenter by remember { mutableStateOf(false) }

    AmbientGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // space for floating bottom bar
        ) {
            // Glass Top Bar
            GlassTopBar(
                title = "MCBD Community",
                subtitle = "Minecraft Bangladesh Hub",
                actions = {
                    if (chatRepository.isDeveloperOrAdmin()) {
                        GlassIconButton(
                            icon = Icons.Default.Terminal,
                            tint = Color(0xFF0284C7),
                            onClick = { showAdminCenter = true }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    GlassIconButton(
                        icon = Icons.Default.Search,
                        onClick = { /* open search */ }
                    )
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // User Welcome Frosted Glass Banner
                item {
                    currentUser?.let { user ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xEAFFFFFF))
                                .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(22.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MinecraftAvatar(
                                    avatarUrl = user.avatarUrl,
                                    ign = user.minecraftIgn,
                                    size = 48.dp,
                                    borderGlow = true
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.fullName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "IGN: ${user.minecraftIgn} • ${user.rank}",
                                        fontSize = 12.sp,
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Community Channels Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMMUNITY CHANNELS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${channels.size} Available",
                            fontSize = 11.sp,
                            color = EmeraldDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Channel Cards in Frosted Glass
                items(channels) { channel ->
                    val isSelected = currentChannel.id == channel.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .shadow(
                                elevation = if (isSelected) 8.dp else 2.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.25f) else Color(0x08000000)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xF2FFFFFF) else Color(0xD9FFFFFF))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                brush = if (isSelected) GlassEmeraldGradient else GlassBorderGradient,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                scope.launch {
                                    chatRepository.selectChannel(channel)
                                    onChannelSelected(channel)
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Channel Icon Badge
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) Color(0x2210B981) else Color(0x10000000)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) EmeraldPrimary.copy(alpha = 0.4f) else Color(0x20000000),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getChannelIcon(channel.id, channel.icon),
                                    contentDescription = null,
                                    tint = if (isSelected) EmeraldPrimary else TextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "# ${channel.name}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldDark else TextPrimary
                                    )
                                    if (channel.isAnnouncement) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFEF3C7))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "OFFICIAL",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }

                                if (!channel.topic.isNullOrEmpty()) {
                                    Text(
                                        text = channel.topic,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            // Enter Arrow Pill
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) EmeraldPrimary else Color(0x15000000))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isSelected) "Open" else "Join",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Community Events Glass Banner
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = EmeraldPrimary.copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xE6FFFFFF),
                                        Color(0xD9E6F9F0)
                                    )
                                )
                            )
                            .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(22.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MCBD Bedwars Championship 2026",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "রেজিস্ট্রেশন চলছে • প্রাইজপুল ৫০,০০০ ডায়মন্ড ও টি-শার্ট",
                                    fontSize = 11.sp,
                                    color = EmeraldDark
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAdminCenter) {
            AdminControlCenterDialog(
                chatRepository = chatRepository,
                onDismiss = { showAdminCenter = false }
            )
        }
    }
}
