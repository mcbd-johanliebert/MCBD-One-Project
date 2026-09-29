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
    val quickReactions = listOf("🔥", "💎", "❤️", "⚔️", "💥", "🇧🇩")

    // Automatically scroll to bottom when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AmbientGlassBackground(modifier = modifier) {
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2010B981))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${channel.icon} ${channel.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
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
                            Text(text = channel.icon, fontSize = 32.sp)
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
                            Text(
                                text = "💬 No messages yet in #${channel.name}\nBe the first to say hello to the community!",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = TextMuted,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isCurrent = (currentUser != null && msg.userId == currentUser?.id) ||
                            (msg.userName == currentUser?.fullName)
                    ChatMessageBubble(
                        message = msg,
                        isCurrentUser = isCurrent,
                        onReactionClick = { emoji ->
                            chatRepository.addReaction(msg.id, emoji)
                        }
                    )
                }
            }

            // Quick Emoji Reaction Bar (Floating Glass Pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickReactions.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, CircleShape, spotColor = Color(0x10000000))
                            .clip(CircleShape)
                            .background(Color(0xE6FFFFFF))
                            .border(1.dp, GlassBorderGradient, CircleShape)
                            .clickable {
                                textInput += " $emoji"
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = emoji, fontSize = 14.sp)
                    }
                }
            }

            // Floating Frosted Glass Message Input Field
            Box(
                modifier = Modifier
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
