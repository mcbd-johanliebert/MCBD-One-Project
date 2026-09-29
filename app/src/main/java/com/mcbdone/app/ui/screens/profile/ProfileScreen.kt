package com.mcbdone.app.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcbdone.app.BuildConfig
import com.mcbdone.app.data.repository.ChatRepository
import com.mcbdone.app.ui.components.*
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    chatRepository: ChatRepository,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by chatRepository.currentUser.collectAsState()

    var ignInput by remember(currentUser) { mutableStateOf(currentUser?.minecraftIgn.orEmpty()) }
    var bioInput by remember(currentUser) { mutableStateOf(currentUser?.bio.orEmpty()) }
    var isEditing by remember { mutableStateOf(false) }

    var showVersionDialog by remember { mutableStateOf(false) }
    var showAdminCenter by remember { mutableStateOf(false) }
    val latestVersion by chatRepository.latestVersion.collectAsState()
    val isUpdateAvailable by chatRepository.isUpdateAvailable.collectAsState()

    AmbientGlassBackground(modifier = modifier) {
        ResponsiveScreenContainer(maxContentWidth = 720.dp) { _, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
            GlassTopBar(
                title = "Minecraft Profile",
                subtitle = "মাইনক্রাফট বাংলাদেশ প্রোফাইল ও সেটিংস"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Profile Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = EmeraldPrimary.copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xEAFFFFFF))
                            .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(28.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar with glowing ring
                            MinecraftAvatar(
                                avatarUrl = currentUser?.avatarUrl,
                                ign = ignInput,
                                size = 84.dp,
                                borderGlow = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = currentUser?.fullName.orEmpty(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            if (!currentUser?.email.isNullOrBlank()) {
                                Text(
                                    text = currentUser?.email.orEmpty(),
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Minecraft Rank Badge
                            MinecraftRankBadge(
                                rank = currentUser?.rank ?: "Member"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Minecraft In-Game Name (IGN) Editor
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x60F1F5F9))
                                    .border(1.dp, Color(0x30000000), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "MINECRAFT IN-GAME NAME (IGN)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (isEditing) {
                                        GlassTextField(
                                            value = ignInput,
                                            onValueChange = { ignInput = it },
                                            placeholder = "Enter your Minecraft IGN"
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        GlassTextField(
                                            value = bioInput,
                                            onValueChange = { bioInput = it },
                                            placeholder = "Your bio"
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        GlassButton(
                                            text = "Save Profile",
                                            icon = Icons.Default.Check,
                                            onClick = {
                                                scope.launch {
                                                    chatRepository.updateProfile(ignInput, bioInput)
                                                    isEditing = false
                                                    Toast.makeText(context, "প্রোফাইল আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = ignInput,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = bioInput,
                                                    fontSize = 12.sp,
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                            IconButton(onClick = { isEditing = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit IGN",
                                                    tint = EmeraldDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Developer & Admin Special Access Card (Always accessible to you)
                item {
                    val isDev = chatRepository.isDeveloperOrAdmin()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0x200284C7))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE))
                                )
                            )
                            .border(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Terminal,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Developer & Admin Access",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0369A1)
                                        )
                                        Text(
                                            text = "Storage, Channel Creator & Badge Delegation",
                                            fontSize = 11.sp,
                                            color = Color(0xFF0284C7)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isDev) "ROOT DEV" else "DEV ACCESS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { showAdminCenter = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Open Admin & Storage Control Center",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }

                            if (!isDev) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            chatRepository.elevateCurrentUserToDeveloper()
                                            Toast.makeText(context, "Developer root access granted!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Authorize as Developer (nazmusshakibshihan@gmail.com)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }
                        }
                    }
                }

                // Supabase & Cloud Auth Integration Info Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xE6FFFFFF))
                            .border(1.dp, GlassBorderGradient, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connected Infrastructure",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            InfoRow(label = "Supabase Project", value = "cvppveogubeudebsmazd.supabase.co")
                            InfoRow(label = "Google Client ID", value = "636820962691-gmsp3j0ju8... apps")
                            InfoRow(label = "Package Name", value = "com.mcbdone.app")
                            InfoRow(
                                label = "Status",
                                value = "Realtime Sync Active",
                                icon = Icons.Default.CheckCircle,
                                iconTint = EmeraldDark
                            )
                        }
                    }
                }

                // Version Control & App Updates Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xE6FFFFFF))
                            .border(1.dp, GlassBorderGradient, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Version Control System",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isUpdateAvailable) Color(0xFFFEF3C7) else Color(0x2010B981))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isUpdateAvailable) Icons.Default.ArrowCircleUp else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isUpdateAvailable) Color(0xFFB45309) else EmeraldDark,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isUpdateAvailable) "Update Available" else "Up to Date",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUpdateAvailable) Color(0xFFB45309) else EmeraldDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            InfoRow(label = "Current App Version", value = "v${chatRepository.currentAppVersion} (Build ${chatRepository.currentVersionCode})")
                            InfoRow(label = "Latest Remote Release", value = "v${latestVersion.versionName} (${latestVersion.releaseDate})")
                            InfoRow(label = "Git Branch / Tag", value = "main @ v1.0.1")

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlassButton(
                                    text = "Check Updates",
                                    icon = Icons.Default.SystemUpdate,
                                    onClick = {
                                        scope.launch {
                                            val (needsUpdate, _) = chatRepository.checkForUpdates()
                                            showVersionDialog = true
                                        }
                                    },
                                    isPrimary = isUpdateAvailable,
                                    modifier = Modifier.weight(1f)
                                )
                                GlassButton(
                                    text = "What's New",
                                    icon = Icons.Default.History,
                                    onClick = { showVersionDialog = true },
                                    isPrimary = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Sign Out Action
                item {
                    GlassButton(
                        text = "Sign Out from MCBD One",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        onClick = {
                            chatRepository.signOut()
                            onSignOut()
                        },
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showVersionDialog) {
            VersionUpdateDialog(
                currentVersion = chatRepository.currentAppVersion,
                versionInfo = latestVersion,
                onDismiss = { showVersionDialog = false }
            )
        }

        if (showAdminCenter) {
            AdminControlCenterDialog(
                chatRepository = chatRepository,
                onDismiss = { showAdminCenter = false }
            )
        }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector? = null,
    iconTint: Color = EmeraldDark
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
