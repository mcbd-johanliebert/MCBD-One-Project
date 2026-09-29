package com.mcbdone.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val email: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("minecraft_ign") val minecraftIgn: String = "",
    val rank: String = "Survivalist",
    val status: String = "online",
    val bio: String = "Minecraft Bangladesh Community Member",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Channel(
    val id: String,
    val name: String,
    val topic: String? = null,
    val icon: String = "chat",
    @SerialName("is_announcement") val isAnnouncement: Boolean = false
) {
    companion object {
        val default = Channel("general", "general", "General Minecraft Bangladesh chat", "chat", false)
    }
}

@Serializable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    @SerialName("channel_id") val channelId: String = "general",
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_name") val userName: String,
    @SerialName("user_avatar") val userAvatar: String? = null,
    @SerialName("minecraft_ign") val minecraftIgn: String = "",
    @SerialName("user_rank") val userRank: String = "Survivalist",
    val content: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("reply_to_id") val replyToId: String? = null,
    @SerialName("reply_to_content") val replyToContent: String? = null,
    @SerialName("reply_to_sender") val replyToSender: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    @SerialName("created_at") val createdAt: String = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
)

@Serializable
data class MinecraftServer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    @SerialName("ip_address") val ipAddress: String,
    val port: Int = 25565,
    val gamemode: String,
    val version: String = "1.20 - 1.21",
    @SerialName("online_players") val onlinePlayers: Int = 0,
    @SerialName("max_players") val maxPlayers: Int = 500,
    @SerialName("ping_ms") val pingMs: Int = 25,
    val verified: Boolean = true,
    val description: String = ""
)

@Serializable
data class ShowcaseItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_name") val userName: String,
    @SerialName("user_avatar") val userAvatar: String? = null,
    @SerialName("minecraft_ign") val minecraftIgn: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("likes_count") val likesCount: Int = 0,
    val category: String = "Mega Build",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AppVersionInfo(
    val id: String = "v1.0.1",
    @SerialName("version_name") val versionName: String = "1.0.1",
    @SerialName("version_code") val versionCode: Int = 2,
    @SerialName("min_supported_version") val minSupportedVersion: String = "1.0.0",
    @SerialName("is_mandatory") val isMandatory: Boolean = false,
    @SerialName("download_url") val downloadUrl: String = "https://github.com/mcbd-johanliebert/MCBD-One-Project/releases/tag/v1.0.1",
    val changelog: List<String> = listOf(
        "MCBD ONE v1.0.1 Release",
        "Awwwards-inspired responsive glassmorphism for phones, foldables, and tablets",
        "Staff badge system with unique vector icons for Developer, Admin, Executive & Moderators",
        "Real-time Database Storage Health Gauge & 500MB quota preventive actions",
        "Root Developer Control Center with member badge delegation"
    ),
    @SerialName("release_date") val releaseDate: String = "September 2026"
)

@Serializable
data class DatabaseStorageStats(
    @SerialName("db_size_bytes") val dbSizeBytes: Long = 0,
    @SerialName("db_size_mb") val dbSizeMb: Double = 0.0,
    @SerialName("max_storage_mb") val maxStorageMb: Double = 500.0,
    @SerialName("storage_percent") val storagePercent: Double = 0.0,
    @SerialName("users_count") val usersCount: Long = 0,
    @SerialName("messages_count") val messagesCount: Long = 0,
    @SerialName("servers_count") val serversCount: Long = 0,
    @SerialName("showcase_count") val showcaseCount: Long = 0,
    val status: String = "HEALTHY"
)

