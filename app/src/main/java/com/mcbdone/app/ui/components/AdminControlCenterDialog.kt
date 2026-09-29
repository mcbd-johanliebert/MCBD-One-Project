package com.mcbdone.app.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mcbdone.app.data.model.Channel
import com.mcbdone.app.data.model.DatabaseStorageStats
import com.mcbdone.app.data.model.MinecraftServer
import com.mcbdone.app.data.model.ShowcaseItem
import com.mcbdone.app.data.model.UserProfile
import com.mcbdone.app.data.repository.ChatRepository
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminControlCenterDialog(
    chatRepository: ChatRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Channels, 1: Servers, 2: Showcase, 3: Staff, 4: Storage, 5: Broadcast
    val channels by chatRepository.channels.collectAsState()
    val servers by chatRepository.servers.collectAsState()
    val showcaseItems by chatRepository.showcase.collectAsState()
    val currentUser by chatRepository.currentUser.collectAsState()
    var storageStats by remember { mutableStateOf(DatabaseStorageStats()) }
    var isLoadingStats by remember { mutableStateOf(true) }

    var usersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoadingUsers by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var targetUserForRole by remember { mutableStateOf<UserProfile?>(null) }
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var showPurgeConfirm by remember { mutableStateOf(false) }
    var showClearChannelsConfirm by remember { mutableStateOf(false) }

    var broadcastText by remember { mutableStateOf("") }
    var isBroadcasting by remember { mutableStateOf(false) }

    // Load initial storage metrics
    LaunchedEffect(Unit) {
        isLoadingStats = true
        storageStats = chatRepository.fetchStorageStats()
        isLoadingStats = false
    }

    // Load users when switching to Staff tab (tab 3)
    LaunchedEffect(selectedTab) {
        if (selectedTab == 3 && usersList.isEmpty()) {
            isLoadingUsers = true
            usersList = chatRepository.fetchAllUsers()
            isLoadingUsers = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x75000000))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = Color(0x30000000))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFAFFFFFF))
                    .border(1.2.dp, GlassBorderGradient, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x180284C7))
                                    .border(1.dp, Color(0xFF0284C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Developer & Admin Control",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                val devEmail = currentUser?.email?.ifBlank { "nazmusshakibshihan@gmail.com" } ?: "nazmusshakibshihan@gmail.com"
                                val isDeveloper = currentUser?.rank == "Developer"
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$devEmail • ${currentUser?.rank ?: "Developer"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDeveloper) Color(0xFF0284C7) else TextSecondary
                                    )
                                    if (!isDeveloper) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(Tap to Elevate)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x1810B981))
                                                .clickable {
                                                    scope.launch {
                                                        chatRepository.elevateCurrentUserToDeveloper()
                                                        Toast.makeText(context, "Elevated to Root Developer!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x15000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Segmented Tabs Pill (6 Full Tabs)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x12000000))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            TabPillButton(
                                title = "Channels",
                                icon = Icons.Default.Tag,
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                        }
                        item {
                            TabPillButton(
                                title = "Servers",
                                icon = Icons.Default.Dns,
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                        }
                        item {
                            TabPillButton(
                                title = "Showcase",
                                icon = Icons.Default.Diamond,
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                        }
                        item {
                            TabPillButton(
                                title = "Staff & Users",
                                icon = Icons.Default.MilitaryTech,
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 }
                            )
                        }
                        item {
                            TabPillButton(
                                title = "Storage Gauge",
                                icon = Icons.Default.PieChart,
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 }
                            )
                        }
                        item {
                            TabPillButton(
                                title = "Broadcast",
                                icon = Icons.Default.Campaign,
                                selected = selectedTab == 5,
                                onClick = { selectedTab = 5 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Content
                    when (selectedTab) {
                        0 -> ChannelsManagerTab(
                            channels = channels,
                            onCreateChannel = { newChannel ->
                                scope.launch {
                                    val res = chatRepository.createChannel(newChannel)
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "#${newChannel.name} তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val err = res.exceptionOrNull()?.message ?: "ত্রুটি হয়েছে"
                                        Toast.makeText(context, "ব্যর্থ: $err", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onDeleteChannel = { channelId ->
                                scope.launch {
                                    val res = chatRepository.deleteChannel(channelId)
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "চ্যানেল মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "মুছে ফেলতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onClearAll = { showClearChannelsConfirm = true }
                        )
                        1 -> ServersManagerTab(
                            servers = servers,
                            onCreateServer = { newServer ->
                                scope.launch {
                                    val res = chatRepository.createServer(newServer)
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "সার্ভার যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "সার্ভার যোগ করতে ব্যর্থ", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDeleteServer = { serverId ->
                                scope.launch {
                                    val res = chatRepository.deleteServer(serverId)
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "সার্ভার মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onToggleVerified = { server ->
                                scope.launch {
                                    chatRepository.toggleServerVerified(server)
                                    Toast.makeText(context, "Verified স্ট্যাটাস আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        2 -> ShowcaseManagerTab(
                            showcaseItems = showcaseItems,
                            onCreateShowcase = { newItem ->
                                scope.launch {
                                    val res = chatRepository.createShowcase(newItem)
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "শোকেস যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "শোকেস যোগ করতে ব্যর্থ", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDeleteShowcase = { itemId ->
                                scope.launch {
                                    val res = chatRepository.deleteShowcase(itemId)
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "শোকেস পোস্ট মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                        3 -> StaffBadgesTab(
                            users = usersList,
                            isLoading = isLoadingUsers,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onSelectUser = { targetUserForRole = it },
                            onDeleteUser = { userToDelete = it },
                            onRefreshUsers = {
                                scope.launch {
                                    isLoadingUsers = true
                                    usersList = chatRepository.fetchAllUsers()
                                    isLoadingUsers = false
                                }
                            }
                        )
                        4 -> StorageMonitorTab(
                            stats = storageStats,
                            isLoading = isLoadingStats,
                            onRefresh = {
                                scope.launch {
                                    isLoadingStats = true
                                    storageStats = chatRepository.fetchStorageStats()
                                    isLoadingStats = false
                                    Toast.makeText(context, "Realtime stats updated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onPurgeMessages = { showPurgeConfirm = true }
                        )
                        5 -> QuickActionsTab(
                            broadcastText = broadcastText,
                            onBroadcastChange = { broadcastText = it },
                            isBroadcasting = isBroadcasting,
                            onSendBroadcast = {
                                if (broadcastText.isNotBlank()) {
                                    scope.launch {
                                        isBroadcasting = true
                                        val res = chatRepository.broadcastAnnouncement(broadcastText)
                                        isBroadcasting = false
                                        if (res.isSuccess) {
                                            broadcastText = ""
                                            Toast.makeText(context, "Announcement broadcasted!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to broadcast", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Role Delegation Picker Dialog
        targetUserForRole?.let { user ->
            RolePickerDialog(
                user = user,
                onDismiss = { targetUserForRole = null },
                onRoleSelected = { newRole ->
                    scope.launch {
                        val res = chatRepository.updateUserRole(user.id, newRole)
                        if (res.isSuccess) {
                            usersList = usersList.map {
                                if (it.id == user.id) it.copy(rank = newRole) else it
                            }
                            Toast.makeText(context, "${user.fullName} is now a $newRole!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update role", Toast.LENGTH_SHORT).show()
                        }
                        targetUserForRole = null
                    }
                }
            )
        }

        // Purge Messages Confirmation Dialog
        if (showPurgeConfirm) {
            AlertDialog(
                onDismissRequest = { showPurgeConfirm = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                },
                title = { Text("Purge Chat Storage?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "This will delete all saved chat messages from the Supabase database to free up storage space. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPurgeConfirm = false
                            scope.launch {
                                val res = chatRepository.purgeOldMessages()
                                if (res.isSuccess) {
                                    storageStats = chatRepository.fetchStorageStats()
                                    Toast.makeText(context, "Database storage freed!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to purge messages", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Purge Now", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPurgeConfirm = false }) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }

        // Clear All Channels Confirmation Dialog
        if (showClearChannelsConfirm) {
            AlertDialog(
                onDismissRequest = { showClearChannelsConfirm = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                },
                title = { Text("Clear All Default Channels?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "This will wipe all existing channels and their message history from Supabase, allowing you to build your clean, custom channel list from scratch.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showClearChannelsConfirm = false
                            scope.launch {
                                val res = chatRepository.clearAllChannels()
                                if (res.isSuccess) {
                                    Toast.makeText(context, "All default channels deleted!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to clear channels", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Wipe All Channels", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearChannelsConfirm = false }) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }

        // Delete User Confirmation Dialog
        userToDelete?.let { user ->
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PersonRemove,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                },
                title = { Text("Delete User Profile?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to delete ${user.fullName} (${user.minecraftIgn}) from the database? This action cannot be undone.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = user
                            userToDelete = null
                            scope.launch {
                                val res = chatRepository.deleteUserProfile(target.id)
                                if (res.isSuccess) {
                                    usersList = usersList.filter { it.id != target.id }
                                    Toast.makeText(context, "${target.fullName} deleted!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Delete Now", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }
    }
}

@Composable
private fun TabPillButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .shadow(if (selected) 4.dp else 0.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) EmeraldDark else TextMuted,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) EmeraldDark else TextMuted
            )
        }
    }
}

@Composable
private fun StorageMonitorTab(
    stats: DatabaseStorageStats,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onPurgeMessages: () -> Unit
) {
    val statusColor = when (stats.status) {
        "CRITICAL" -> Color(0xFFDC2626)
        "WARNING" -> Color(0xFFD97706)
        else -> Color(0xFF059669)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Storage Gauge Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0x18000000), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DATABASE QUOTA (SUPABASE FREE TIER)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (stats.status == "HEALTHY") Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stats.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${stats.dbSizeMb} MB",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "of 500.0 MB Max Storage Capacity",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = "${stats.storagePercent}% Used",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Linear Storage Bar
                    LinearProgressIndicator(
                        progress = { (stats.storagePercent / 100f).toFloat().coerceIn(0.01f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = Color(0x20000000)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Refresh Button
                    Button(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Querying Supabase...", fontSize = 12.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh Realtime Storage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Row Counts Grid
        item {
            Text(
                text = "LIVE TABLE ROW METRICS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Users",
                    value = "${stats.usersCount}",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Messages",
                    value = "${stats.messagesCount}",
                    icon = Icons.Default.Chat,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Servers",
                    value = "${stats.serversCount}",
                    icon = Icons.Default.Dns,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Showcase",
                    value = "${stats.showcaseCount}",
                    icon = Icons.Default.Architecture,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action card: Preventive maintenance
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SecurityUpdateWarning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Storage Action (Before Database Is Full)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "If messages accumulate or free quota approaches 80%, clear message history to immediately release disk space.",
                        fontSize = 11.sp,
                        color = Color(0xFF7F1D1D),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onPurgeMessages,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Purge Chat History to Free Storage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0x18000000), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StaffBadgesTab(
    users: List<UserProfile>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectUser: (UserProfile) -> Unit,
    onDeleteUser: (UserProfile) -> Unit,
    onRefreshUsers: () -> Unit
) {
    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.minecraftIgn.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input & Refresh Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                GlassTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = "Search user by name or IGN...",
                    leadingIcon = Icons.Default.Search
                )
            }
            IconButton(
                onClick = onRefreshUsers,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x15000000))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Users",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "DELEGATE UNIQUE BADGES (ADMIN, EXEC, SENIOR MOD, MOD)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No users found in database",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0x18000000), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                MinecraftAvatar(
                                    avatarUrl = user.avatarUrl,
                                    ign = user.minecraftIgn,
                                    size = 38.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = user.fullName.ifEmpty { "MCBD User" },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "IGN: ${user.minecraftIgn} • ${user.email}",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    MinecraftRankBadge(rank = user.rank)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onSelectUser(user) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Assign", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { onDeleteUser(user) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1AEF4444))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete User",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RolePickerDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onRoleSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Staff Badge",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = "Select a badge for ${user.fullName} (${user.minecraftIgn})",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                ASSIGNABLE_ROLES.forEach { role ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, role.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { onRoleSelected(role.id) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(role.primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = role.icon,
                                    contentDescription = null,
                                    tint = role.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = role.description,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsTab(
    broadcastText: String,
    onBroadcastChange: (String) -> Unit,
    isBroadcasting: Boolean,
    onSendBroadcast: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0x18000000), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Broadcast Official Announcement",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sends a pinned announcement directly into the #announcements channel for all community players.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = broadcastText,
                        onValueChange = onBroadcastChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Write official community / server announcement...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onSendBroadcast,
                        enabled = broadcastText.isNotBlank() && !isBroadcasting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isBroadcasting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Post to #announcements", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelsManagerTab(
    channels: List<Channel>,
    onCreateChannel: (Channel) -> Unit,
    onDeleteChannel: (String) -> Unit,
    onClearAll: () -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var channelTopic by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("chat") }
    var isAnnouncement by remember { mutableStateOf(false) }

    val iconOptions = listOf(
        Pair("chat", Icons.Default.ChatBubbleOutline),
        Pair("campaign", Icons.Default.Campaign),
        Pair("shield", Icons.Default.Shield),
        Pair("architecture", Icons.Default.Architecture),
        Pair("dns", Icons.Default.Dns),
        Pair("sports", Icons.Default.SportsEsports),
        Pair("diamond", Icons.Default.Diamond),
        Pair("terminal", Icons.Default.Terminal)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Actions Banner (Clear All Default Channels)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ডিফল্ট চ্যানেল পরিষ্কার করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "সব ডামি চ্যানেল ডিলিট করে সম্পূর্ণ নতুন চ্যানেল তৈরি করুন",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C)
                        )
                    }
                    Button(
                        onClick = onClearAll,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Create Channel Form Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.2.dp, Color(0x18000000), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create New Channel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "CHANNEL NAME / SLUG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = channelName,
                        onValueChange = { 
                            channelName = it.lowercase().replace(" ", "-").filter { c -> c.isLetterOrDigit() || c == '-' }
                        },
                        placeholder = { Text("e.g. general, announcements, pvp-talk", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "CHANNEL TOPIC / DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = channelTopic,
                        onValueChange = { channelTopic = it },
                        placeholder = { Text("e.g. Official community chat for Bangladeshi Minecraft players", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "SELECT ICON", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        iconOptions.forEach { (iconKey, vector) ->
                            val isSelected = selectedIcon == iconKey
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) EmeraldPrimary else Color(0x15000000))
                                    .border(if (isSelected) 1.5.dp else 0.dp, EmeraldDark, CircleShape)
                                    .clickable { selectedIcon = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = iconKey,
                                    tint = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x0C000000))
                            .clickable { isAnnouncement = !isAnnouncement }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Announcement Channel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Only Staff/Developers can post messages",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isAnnouncement,
                            onCheckedChange = { isAnnouncement = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val cleanName = channelName.trim()
                            if (cleanName.isNotBlank()) {
                                onCreateChannel(
                                    Channel(
                                        id = cleanName,
                                        name = cleanName,
                                        topic = channelTopic.ifBlank { "Minecraft Bangladesh Community Channel" },
                                        icon = selectedIcon,
                                        isAnnouncement = isAnnouncement
                                    )
                                )
                                channelName = ""
                                channelTopic = ""
                                isAnnouncement = false
                            }
                        },
                        enabled = channelName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Channel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Active Channels List Section
        item {
            Text(
                text = "ACTIVE CHANNELS (${channels.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        }

        if (channels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No channels in database. Create your first channel above!",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(channels, key = { it.id }) { channel ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0x18000000), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getChannelIcon(channel.id, channel.icon),
                                    contentDescription = null,
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "# ${channel.name}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (channel.isAnnouncement) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0x20F59E0B))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Announce",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = channel.topic ?: "ID: ${channel.id}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteChannel(channel.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AEF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServersManagerTab(
    servers: List<MinecraftServer>,
    onCreateServer: (MinecraftServer) -> Unit,
    onDeleteServer: (String) -> Unit,
    onToggleVerified: (MinecraftServer) -> Unit
) {
    var serverName by remember { mutableStateOf("") }
    var serverIp by remember { mutableStateOf("") }
    var serverPort by remember { mutableStateOf("25565") }
    var serverGamemode by remember { mutableStateOf("Survival / SMP") }
    var serverVersion by remember { mutableStateOf("1.20 - 1.21") }
    var serverDesc by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Create Server Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.2.dp, Color(0x18000000), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Minecraft Server",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "SERVER NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = serverName,
                        onValueChange = { serverName = it },
                        placeholder = { Text("e.g. MCBD Official SMP", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(text = "IP ADDRESS / DOMAIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = serverIp,
                                onValueChange = { serverIp = it.trim() },
                                placeholder = { Text("play.mcbd.net", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "PORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = serverPort,
                                onValueChange = { serverPort = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("25565", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "GAMEMODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = serverGamemode,
                                onValueChange = { serverGamemode = it },
                                placeholder = { Text("SMP, Bedwars, Skyblock", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "VERSION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = serverVersion,
                                onValueChange = { serverVersion = it },
                                placeholder = { Text("1.20 - 1.21", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = serverDesc,
                        onValueChange = { serverDesc = it },
                        placeholder = { Text("Bangladeshi low ping server with custom quests", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x0C000000))
                            .clickable { isVerified = !isVerified }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Verified MCBD Server", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Displays official verified checkmark", fontSize = 10.sp, color = TextSecondary)
                        }
                        Switch(checked = isVerified, onCheckedChange = { isVerified = it })
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (serverName.isNotBlank() && serverIp.isNotBlank()) {
                                onCreateServer(
                                    MinecraftServer(
                                        name = serverName.trim(),
                                        ipAddress = serverIp.trim(),
                                        port = serverPort.toIntOrNull() ?: 25565,
                                        gamemode = serverGamemode.ifBlank { "Survival" },
                                        version = serverVersion.ifBlank { "1.20 - 1.21" },
                                        description = serverDesc.trim(),
                                        verified = isVerified
                                    )
                                )
                                serverName = ""
                                serverIp = ""
                                serverDesc = ""
                            }
                        },
                        enabled = serverName.isNotBlank() && serverIp.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Server", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Active Servers List Section
        item {
            Text(
                text = "ACTIVE SERVERS (${servers.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        }

        if (servers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No servers listed. Add the first one above!", fontSize = 12.sp, color = TextMuted)
                }
            }
        } else {
            items(servers, key = { it.id }) { srv ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0x18000000), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = srv.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                if (srv.verified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${srv.ipAddress}:${srv.port} • ${srv.gamemode} (${srv.version})",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { onToggleVerified(srv) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (srv.verified) Color(0x1A0284C7) else Color(0x15000000))
                            ) {
                                Icon(
                                    imageVector = if (srv.verified) Icons.Default.Verified else Icons.Default.CheckCircle,
                                    contentDescription = "Toggle Verified",
                                    tint = if (srv.verified) Color(0xFF0284C7) else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteServer(srv.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1AEF4444))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Server",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseManagerTab(
    showcaseItems: List<ShowcaseItem>,
    onCreateShowcase: (ShowcaseItem) -> Unit,
    onDeleteShowcase: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mega Build") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var authorIgn by remember { mutableStateOf("") }

    val categories = listOf("Mega Build", "Redstone", "Survival Base", "Pixel Art", "Minigame", "Castle")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Create Showcase Form Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.2.dp, Color(0x18000000), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Showcase Build",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "BUILD TITLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Lalbagh Fort in Minecraft", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "IMAGE DIRECT URL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it.trim() },
                        placeholder = { Text("https://images.unsplash.com/... or i.imgur.com/...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "CREATOR IGN / NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = authorIgn,
                        onValueChange = { authorIgn = it },
                        placeholder = { Text("e.g. Shakib_MCBD", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "CATEGORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSelected = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldPrimary else Color(0x12000000))
                                    .clickable { category = cat }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Details about the build, shaders, time spent...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && imageUrl.isNotBlank()) {
                                onCreateShowcase(
                                    ShowcaseItem(
                                        title = title.trim(),
                                        imageUrl = imageUrl.trim(),
                                        userName = authorIgn.ifBlank { "MCBD Builder" },
                                        minecraftIgn = authorIgn.ifBlank { "MCBD_Player" },
                                        category = category,
                                        description = description.trim()
                                    )
                                )
                                title = ""
                                imageUrl = ""
                                description = ""
                            }
                        },
                        enabled = title.isNotBlank() && imageUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Build", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Active Showcase Items List
        item {
            Text(
                text = "ACTIVE SHOWCASE POSTS (${showcaseItems.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        }

        if (showcaseItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No showcase builds listed yet. Add one above!", fontSize = 12.sp, color = TextMuted)
                }
            }
        } else {
            items(showcaseItems, key = { it.id }) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0x18000000), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "By ${item.userName} • ${item.category} • ❤️ ${item.likesCount}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onDeleteShowcase(item.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AEF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Showcase",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
