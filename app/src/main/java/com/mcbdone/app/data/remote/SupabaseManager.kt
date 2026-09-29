package com.mcbdone.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.mcbdone.app.BuildConfig
import com.mcbdone.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SupabaseManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("mcbd_supabase_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val baseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    var currentSessionToken: String?
        get() = prefs.getString("access_token", null)
        set(value) = prefs.edit().putString("access_token", value).apply()

    var currentUser: UserProfile?
        get() {
            val raw = prefs.getString("current_user", null) ?: return null
            return try {
                val profile = json.decodeFromString<UserProfile>(raw)
                // Filter out any fake, guest, or demo profiles from previous sessions
                if (profile.id.startsWith("guest_") ||
                    profile.id.startsWith("google_") ||
                    profile.email == "community@mcbd.net" ||
                    profile.email == "guest@mcbd.net" ||
                    profile.fullName == "Minecraft BD Player" ||
                    profile.fullName == "BD Adventurer" ||
                    profile.fullName == "BD Miner"
                ) {
                    prefs.edit().remove("current_user").remove("access_token").apply()
                    null
                } else {
                    profile
                }
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove("current_user").apply()
            } else {
                prefs.edit().putString("current_user", json.encodeToString(value)).apply()
            }
        }

    fun isUserLoggedIn(): Boolean = currentUser != null

    suspend fun exchangeGoogleToken(idToken: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/auth/v1/token?grant_type=id_token"
            val payload = """
                {
                    "provider": "google",
                    "id_token": "$idToken",
                    "client_id": "${BuildConfig.GOOGLE_WEB_CLIENT_ID}"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val jsonObj = json.parseToJsonElement(body).jsonObject
                val accessToken = jsonObj["access_token"]?.jsonPrimitive?.content
                val userObj = jsonObj["user"]?.jsonObject

                val userId = userObj?.get("id")?.jsonPrimitive?.content ?: java.util.UUID.randomUUID().toString()
                val email = userObj?.get("email")?.jsonPrimitive?.content ?: ""
                val metadata = userObj?.get("user_metadata")?.jsonObject
                val fullName = metadata?.get("full_name")?.jsonPrimitive?.content
                    ?: metadata?.get("name")?.jsonPrimitive?.content ?: "MCBD Member"
                val avatar = metadata?.get("avatar_url")?.jsonPrimitive?.content
                    ?: "https://crafthead.net/helm/Steve"

                val ign = fullName.replace(" ", "_").take(16)
                val isDeveloper = email.trim().equals("nazmusshakibshihan@gmail.com", ignoreCase = true)
                val defaultRank = if (isDeveloper) "Developer" else "Member"
                val defaultBio = if (isDeveloper) "Lead Developer & System Architect" else "Minecraft Bangladesh Community Member"

                val profile = UserProfile(
                    id = userId,
                    email = email,
                    fullName = fullName,
                    avatarUrl = avatar,
                    minecraftIgn = ign,
                    rank = defaultRank,
                    status = "online",
                    bio = defaultBio
                )

                currentSessionToken = accessToken
                currentUser = profile
                upsertProfile(profile)
                Result.success(profile)
            } else {
                Log.e("SupabaseManager", "Auth exchange API response error: $body")
                Result.failure(Exception("Supabase Auth error ($response.code): Check that Google Provider is enabled in Supabase Dashboard"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error exchanging token", e)
            Result.failure(e)
        }
    }

    suspend fun upsertProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/profiles"
            val body = json.encodeToString(profile)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Upsert profile error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/channels?select=*"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val channels = json.decodeFromString<List<Channel>>(body)
                    if (channels.isNotEmpty()) return@withContext channels
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Using fallback channels", e)
        }
        fallbackChannels
    }

    suspend fun fetchMessages(channelId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/messages?channel_id=eq.$channelId&order=created_at.asc&limit=100"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val messages = json.decodeFromString<List<ChatMessage>>(body)
                    if (messages.isNotEmpty()) return@withContext messages
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Using fallback messages for $channelId", e)
        }
        fallbackMessages.filter { it.channelId == channelId }
    }

    suspend fun sendMessage(message: ChatMessage): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/messages"
            val body = json.encodeToString(message)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Prefer", "return=representation")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(message)
                } else {
                    Log.w("SupabaseManager", "Message send remote error: ${response.code}")
                    Result.success(message) // Return success so UI updates locally
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Message fallback sent locally", e)
            Result.success(message)
        }
    }

    suspend fun fetchServers(): List<MinecraftServer> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/servers?select=*&order=online_players.desc"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val list = json.decodeFromString<List<MinecraftServer>>(body)
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Using fallback servers", e)
        }
        fallbackServers
    }

    suspend fun fetchShowcase(): List<ShowcaseItem> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/showcase_posts?select=*&order=likes_count.desc"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val list = json.decodeFromString<List<ShowcaseItem>>(body)
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Using fallback showcase", e)
        }
        fallbackShowcase
    }

    suspend fun fetchLatestVersion(): AppVersionInfo = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/app_versions?order=version_code.desc&limit=1"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val list = json.decodeFromString<List<AppVersionInfo>>(body)
                    if (list.isNotEmpty()) return@withContext list.first()
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Using fallback version info", e)
        }
        fallbackVersion
    }

    suspend fun fetchUsers(): List<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/profiles?select=*&order=created_at.desc"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return@withContext json.decodeFromString<List<UserProfile>>(body)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "fetchUsers error", e)
        }
        emptyList()
    }

    suspend fun updateUserRank(userId: String, newRank: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/profiles?id=eq.$userId"
            val payload = """{"rank":"$newRank"}"""
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Content-Type", "application/json")
                .patch(payload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Failed to update rank HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchDatabaseStats(): DatabaseStorageStats = withContext(Dispatchers.IO) {
        // 1. Try PostgreSQL RPC
        try {
            val rpcUrl = "$baseUrl/rest/v1/rpc/get_database_stats"
            val request = Request.Builder()
                .url(rpcUrl)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    if (body.isNotEmpty() && body.startsWith("{")) {
                        return@withContext json.decodeFromString<DatabaseStorageStats>(body)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "RPC get_database_stats fallback", e)
        }

        // 2. Query table counts directly using Supabase count=exact
        suspend fun countTable(table: String): Long {
            return try {
                val url = "$baseUrl/rest/v1/$table?select=id"
                val req = Request.Builder()
                    .url(url)
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                    .addHeader("Range", "0-0")
                    .addHeader("Prefer", "count=exact")
                    .get()
                    .build()
                client.newCall(req).execute().use { res ->
                    val cr = res.header("Content-Range")
                    cr?.substringAfter("/")?.toLongOrNull() ?: 0L
                }
            } catch (_: Exception) { 0L }
        }

        val usersCount = countTable("profiles")
        val messagesCount = countTable("messages")
        val serversCount = countTable("servers")
        val showcaseCount = countTable("showcase_posts")

        val estimatedBytes = (usersCount * 4096) + (messagesCount * 1024) + (serversCount * 2048) + (showcaseCount * 4096) + 8388608 // ~8MB base catalog
        val mb = Math.round((estimatedBytes / (1024.0 * 1024.0)) * 100.0) / 100.0
        val maxMb = 500.0
        val percent = Math.round(((mb / maxMb) * 100.0) * 10.0) / 10.0
        val status = when {
            percent >= 85.0 -> "CRITICAL"
            percent >= 65.0 -> "WARNING"
            else -> "HEALTHY"
        }

        DatabaseStorageStats(
            dbSizeBytes = estimatedBytes,
            dbSizeMb = mb,
            maxStorageMb = maxMb,
            storagePercent = percent,
            usersCount = usersCount,
            messagesCount = messagesCount,
            serversCount = serversCount,
            showcaseCount = showcaseCount,
            status = status
        )
    }

    suspend fun purgeOldMessages(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/messages"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(1)
                else Result.failure(Exception("Failed to delete messages HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        currentSessionToken = null
        currentUser = null
        prefs.edit().clear().apply()
    }

    companion object {
        val fallbackVersion = AppVersionInfo(
            versionName = "1.0.0",
            versionCode = 1,
            minSupportedVersion = "1.0.0",
            isMandatory = false,
            downloadUrl = "https://github.com/mcbd-johanliebert/MCBD-One-Project/releases/tag/v1.0.0",
            changelog = listOf(
                "Official launch of MCBD ONE (v1.0.0)",
                "White Themed Glassmorphic UI with dynamic light refraction",
                "Custom Minecraft 3D skin heads & rank system",
                "Bangladesh verified server list with live ping & player count",
                "Realtime community channels with vector icon reactions",
                "Google Auth & Supabase Realtime synchronization"
            ),
            releaseDate = "September 2026"
        )

        val fallbackChannels = listOf(
            Channel("announcements", "announcements", "অফিশিয়াল বিডি টুর্নামেন্ট ও সার্ভার আপডেট", "campaign", true),
            Channel("general", "general-chat", "বাংলাদেশি মাইনক্রাফটারদের আড্ডা ও খোশগল্প", "chat", false),
            Channel("pvp-bedwars", "pvp-and-bedwars", "বেডওয়ার্স স্কোয়াড ও পিভিপি ট্রিক্স", "shield", false),
            Channel("builds-redstone", "builds-and-redstone", "অসাধারণ বিল্ড ও রেডস্টোন মেশিনারি শেয়ার", "architecture", false),
            Channel("bd-servers", "bd-server-ips", "বাংলাদেশি সেরা সার্ভার আইপি ও লিস্ট", "dns", false)
        )

        val fallbackMessages = emptyList<ChatMessage>()
        val fallbackServers = emptyList<MinecraftServer>()
        val fallbackShowcase = emptyList<ShowcaseItem>()
    }
}
