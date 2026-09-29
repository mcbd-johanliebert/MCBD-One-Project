package com.mcbdone.app.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mcbdone.app.data.model.DatabaseStorageStats
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

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Storage, 1: Badges, 2: Quick Broadcast
    var storageStats by remember { mutableStateOf(DatabaseStorageStats()) }
    var isLoadingStats by remember { mutableStateOf(true) }

    var usersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoadingUsers by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var targetUserForRole by remember { mutableStateOf<UserProfile?>(null) }
    var showPurgeConfirm by remember { mutableStateOf(false) }

    var broadcastText by remember { mutableStateOf("") }
    var isBroadcasting by remember { mutableStateOf(false) }

    // Load initial storage metrics
    LaunchedEffect(Unit) {
        isLoadingStats = true
        storageStats = chatRepository.fetchStorageStats()
        isLoadingStats = false
    }

    // Load users when switching to Badges tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && usersList.isEmpty()) {
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
                                    text = "Admin Control Center",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "nazmusshakibshihan@gmail.com • Root Dev",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
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

                    // Segmented Tabs Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x12000000))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TabPillButton(
                            title = "Storage Monitor",
                            icon = Icons.Default.PieChart,
                            selected = selectedTab == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        TabPillButton(
                            title = "Staff Badges",
                            icon = Icons.Default.MilitaryTech,
                            selected = selectedTab == 1,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                        TabPillButton(
                            title = "Quick Actions",
                            icon = Icons.Default.Campaign,
                            selected = selectedTab == 2,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 2 }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Content
                    when (selectedTab) {
                        0 -> StorageMonitorTab(
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
                        1 -> StaffBadgesTab(
                            users = usersList,
                            isLoading = isLoadingUsers,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onSelectUser = { targetUserForRole = it },
                            onRefreshUsers = {
                                scope.launch {
                                    isLoadingUsers = true
                                    usersList = chatRepository.fetchAllUsers()
                                    isLoadingUsers = false
                                }
                            }
                        )
                        2 -> QuickActionsTab(
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
                        placeholder = { Text("Write official tournament / server announcement...", fontSize = 12.sp) },
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
