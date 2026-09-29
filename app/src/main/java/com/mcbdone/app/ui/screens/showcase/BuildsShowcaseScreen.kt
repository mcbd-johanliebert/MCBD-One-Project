package com.mcbdone.app.ui.screens.showcase

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mcbdone.app.data.model.ShowcaseItem
import com.mcbdone.app.data.repository.ChatRepository
import com.mcbdone.app.ui.components.GlassTopBar
import com.mcbdone.app.ui.components.MinecraftAvatar
import com.mcbdone.app.ui.theme.*

@Composable
fun BuildsShowcaseScreen(
    chatRepository: ChatRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showcaseItems by chatRepository.showcase.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Mega Build", "Redstone Farm", "Survival", "Pixel Art")

    AmbientGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            GlassTopBar(
                title = "Builds & Redstone",
                subtitle = "বাংলাদেশি মাইনক্রাফটারদের সৃষ্টিশীল কাজ"
            )

            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .shadow(if (isSelected) 4.dp else 0.dp, CircleShape, spotColor = EmeraldPrimary.copy(alpha = 0.3f))
                            .clip(CircleShape)
                            .background(if (isSelected) EmeraldPrimary else Color(0xD9FFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) SolidColor(Color.White.copy(alpha = 0.6f)) else GlassBorderGradient,
                                CircleShape
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            val filtered = if (selectedCategory == "All") showcaseItems
            else showcaseItems.filter { it.category == selectedCategory }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xEAFFFFFF))
                            .border(1.dp, GlassBorderGradient, RoundedCornerShape(24.dp))
                            .padding(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Architecture,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Builds in this Category Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share your creations with the Bangladeshi Minecraft community!",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered) { item ->
                        ShowcaseCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseCard(item: ShowcaseItem) {
    var isLiked by remember { mutableStateOf(false) }
    var likes by remember { mutableIntStateOf(item.likesCount) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(26.dp), spotColor = Color(0x15000000))
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xEBFFFFFF))
            .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(26.dp))
    ) {
        Column {
            // Builder Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MinecraftAvatar(
                    avatarUrl = item.userAvatar,
                    ign = item.minecraftIgn ?: "Steve",
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = item.minecraftIgn ?: "Minecraft BD Player",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1810B981))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldDark
                    )
                }
            }

            // Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE2E8F0))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Info: Title, Description & Like Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (!item.description.isNullOrEmpty()) {
                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Likes Action Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isLiked) Color(0x20EF4444) else Color(0x10000000))
                            .clickable {
                                isLiked = !isLiked
                                likes += if (isLiked) 1 else -1
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) RedstoneAccent else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$likes Likes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLiked) RedstoneAccent else TextPrimary
                        )
                    }

                    Text(
                        text = "Minecraft 1.21 BD",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
