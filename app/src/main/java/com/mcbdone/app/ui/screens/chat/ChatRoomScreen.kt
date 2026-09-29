package com.mcbdone.app.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
fun ChatRoomScreen(
    channel: Channel,
    chatRepository: ChatRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val messages by chatRepository.messages.collectAsState()
    val currentUser by chatRepository.currentUser.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val quickReactionIcons = listOf(
        Pair("like", Icons.Default.ThumbUp),
        Pair("love", Icons.Default.Favorite),
        Pair("star", Icons.Default.Star),
        Pair("bolt", Icons.Default.Bolt),
        Pair("fire", Icons.Default.Whatshot),
        Pair("shield", Icons.Default.Shield)
    )

    // Automatically scroll to bottom when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AmbientGlassBackground(modifier = modifier) {
        ResponsiveScreenContainer(maxContentWidth = 780.dp) { _, _ ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Floating Glass Top Header
            GlassTopBar(
                title = "# ${channel.name}",
                subtitle = channel.topic ?: "Minecraft Bangladesh Chat",
                navigationIcon = {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack,
                        size = 38.dp
                    )
                },
                actions = {
                    if (channel.isAnnouncement) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1DF59E0B))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Announcements",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            )

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Channel Topic Header Card in Chat
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xCCFFFFFF))
                            .border(1.dp, GlassBorderGradient, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = getChannelIcon(channel.id, channel.icon),
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Welcome to #${channel.name}!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = channel.topic ?: "Start the discussion with fellow Bangladeshi Minecrafters.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No messages yet in #${channel.name}\nBe the first to say hello to the community!",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isCurrent = (currentUser != null && msg.userId == currentUser?.id) ||
                            (msg.userName == currentUser?.fullName)
                    ChatMessageBubble(
                        message = msg,
                        isCurrentUser = isCurrent,
                        onReactionClick = { reactionKey ->
                            chatRepository.addReaction(msg.id, reactionKey)
                        }
                    )
                }
            }

            // Quick Vector Reaction Bar (Floating Glass Pill - Icons Only)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickReactionIcons.forEach { (reactionKey, iconVector) ->
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, CircleShape, spotColor = Color(0x10000000))
                            .clip(CircleShape)
                            .background(Color(0xE6FFFFFF))
                            .border(1.dp, GlassBorderGradient, CircleShape)
                            .clickable {
                                messages.lastOrNull()?.let { lastMsg ->
                                    chatRepository.addReaction(lastMsg.id, reactionKey)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = reactionKey,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Floating Frosted Glass Message Input Field
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0x18000000))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xF0FFFFFF))
                    .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment Icon
                    IconButton(
                        onClick = { /* image attach */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach screenshot",
                            tint = EmeraldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Text Input
                    GlassTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = "Message #${channel.name}...",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button with Emerald Glow
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape, spotColor = EmeraldPrimary.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        EmeraldPrimary,
                                        Color(0xFF059669)
                                    )
                                )
                            )
                            .border(1.dp, Color(0x80FFFFFF), CircleShape)
                            .clickable(enabled = textInput.isNotBlank()) {
                                val content = textInput.trim()
                                textInput = ""
                                scope.launch {
                                    chatRepository.sendMessage(content)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
}
