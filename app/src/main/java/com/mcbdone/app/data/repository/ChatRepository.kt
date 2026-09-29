package com.mcbdone.app.data.repository

import android.content.Context
import com.mcbdone.app.data.model.*
import com.mcbdone.app.data.remote.SupabaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository(context: Context) {

    private val supabase = SupabaseManager(context)

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _currentChannel = MutableStateFlow(SupabaseManager.fallbackChannels.first())
    val currentChannel: StateFlow<Channel> = _currentChannel.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _servers = MutableStateFlow<List<MinecraftServer>>(emptyList())
    val servers: StateFlow<List<MinecraftServer>> = _servers.asStateFlow()

    private val _showcase = MutableStateFlow<List<ShowcaseItem>>(emptyList())
    val showcase: StateFlow<List<ShowcaseItem>> = _showcase.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(supabase.currentUser)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    val currentAppVersion = "1.0.0"
    val currentVersionCode = 1

    private val _latestVersion = MutableStateFlow<AppVersionInfo>(SupabaseManager.fallbackVersion)
    val latestVersion: StateFlow<AppVersionInfo> = _latestVersion.asStateFlow()

    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable.asStateFlow()

    private val _isStrictUpdateRequired = MutableStateFlow(false)
    val isStrictUpdateRequired: StateFlow<Boolean> = _isStrictUpdateRequired.asStateFlow()

    suspend fun loadInitialData() {
        val chList = supabase.fetchChannels()
        _channels.value = chList
        if (chList.isNotEmpty()) {
            _currentChannel.value = chList.first()
            loadMessages(chList.first().id)
        }
        _servers.value = supabase.fetchServers()
        _showcase.value = supabase.fetchShowcase()
        _currentUser.value = supabase.currentUser
        checkForUpdates()
    }

    suspend fun checkForUpdates(): Pair<Boolean, AppVersionInfo> {
        val remoteVersion = supabase.fetchLatestVersion()
        _latestVersion.value = remoteVersion
        val needsUpdate = remoteVersion.versionCode > currentVersionCode
        _isUpdateAvailable.value = needsUpdate
        _isStrictUpdateRequired.value = needsUpdate
        return Pair(needsUpdate, remoteVersion)
    }

    suspend fun selectChannel(channel: Channel) {
        _currentChannel.value = channel
        loadMessages(channel.id)
    }

    suspend fun loadMessages(channelId: String) {
        val msgs = supabase.fetchMessages(channelId)
        _messages.value = msgs
    }

    suspend fun sendMessage(
        content: String,
        replyTo: ChatMessage? = null,
        imageUrl: String? = null
    ): Result<ChatMessage> {
        val user = _currentUser.value ?: return Result.failure(Exception("You must be logged in to send messages"))

        val newMsg = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            channelId = _currentChannel.value.id,
            userId = user.id,
            userName = user.fullName,
            userAvatar = user.avatarUrl,
            minecraftIgn = user.minecraftIgn,
            userRank = user.rank,
            content = content,
            imageUrl = imageUrl,
            replyToId = replyTo?.id,
            replyToContent = replyTo?.content,
            replyToSender = replyTo?.userName,
            createdAt = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )

        // Optimistically add to UI
        _messages.value = _messages.value + newMsg
        return supabase.sendMessage(newMsg)
    }

    fun addReaction(messageId: String, emoji: String) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                val updatedReactions = msg.reactions.toMutableMap()
                val current = updatedReactions[emoji] ?: 0
                updatedReactions[emoji] = current + 1
                msg.copy(reactions = updatedReactions)
            } else {
                msg
            }
        }
    }

    suspend fun onGoogleLoginSuccess(idToken: String): Result<UserProfile> {
        val res = supabase.exchangeGoogleToken(idToken)
        if (res.isSuccess) {
            _currentUser.value = res.getOrNull()
        }
        return res
    }

    fun signOut() {
        supabase.signOut()
        _currentUser.value = null
    }

    suspend fun updateProfile(ign: String, bio: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            minecraftIgn = ign,
            bio = bio,
            avatarUrl = "https://crafthead.net/helm/$ign"
        )
        _currentUser.value = updated
        supabase.currentUser = updated
        supabase.upsertProfile(updated)
    }

    fun isDeveloperOrAdmin(): Boolean {
        val user = _currentUser.value ?: return false
        val email = user.email.trim().lowercase()
        return email == "nazmusshakibshihan@gmail.com" ||
               user.rank.equals("Developer", ignoreCase = true) ||
               user.rank.equals("Admin", ignoreCase = true)
    }

    suspend fun fetchAllUsers(): List<UserProfile> {
        return supabase.fetchUsers()
    }

    suspend fun updateUserRole(userId: String, newRank: String): Result<Unit> {
        val res = supabase.updateUserRank(userId, newRank)
        if (res.isSuccess) {
            if (_currentUser.value?.id == userId) {
                val updated = _currentUser.value!!.copy(rank = newRank)
                _currentUser.value = updated
                supabase.currentUser = updated
            }
        }
        return res
    }

    suspend fun fetchStorageStats(): DatabaseStorageStats {
        return supabase.fetchDatabaseStats()
    }

    suspend fun purgeOldMessages(): Result<Int> {
        val res = supabase.purgeOldMessages()
        if (res.isSuccess) {
            _messages.value = emptyList()
        }
        return res
    }

    suspend fun broadcastAnnouncement(content: String): Result<ChatMessage> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        val announcement = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            channelId = "announcements",
            userId = user.id,
            userName = user.fullName,
            userAvatar = user.avatarUrl,
            minecraftIgn = user.minecraftIgn,
            userRank = user.rank,
            content = content,
            createdAt = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
        return supabase.sendMessage(announcement)
    }
}
