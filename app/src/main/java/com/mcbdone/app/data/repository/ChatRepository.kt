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

    fun triggerStrictUpdateDemo(enable: Boolean) {
        if (enable) {
            _latestVersion.value = AppVersionInfo(
                versionName = "1.0.1",
                versionCode = 2,
                minSupportedVersion = "1.0.1",
                isMandatory = true,
                downloadUrl = "https://cvppveogubeudebsmazd.supabase.co/storage/v1/object/public/updates/app-debug.apk",
                changelog = listOf(
                    "🔥 Mandatory Security & Server protocol upgrade",
                    "💎 Faster Realtime Chat engine & voice note preview",
                    "⚔️ Low-latency ping optimizations for BD Bedwars"
                )
            )
            _isUpdateAvailable.value = true
            _isStrictUpdateRequired.value = true
        } else {
            _isStrictUpdateRequired.value = false
        }
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
        val user = _currentUser.value ?: UserProfile(
            fullName = "BD Miner",
            avatarUrl = "https://crafthead.net/helm/Steve",
            minecraftIgn = "BD_Player",
            rank = "⛏️ Survivalist"
        )

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
}
