package com.mcbdone.app.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

sealed interface DownloadStatus {
    object Idle : DownloadStatus
    data class Downloading(
        val progress: Float,
        val percentage: Int,
        val downloadedMb: Float,
        val totalMb: Float
    ) : DownloadStatus
    data class Completed(val apkFile: File) : DownloadStatus
    data class Error(val message: String) : DownloadStatus
}

class AppUpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun downloadAndTrackApk(downloadUrl: String): Flow<DownloadStatus> = flow {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val targetFile = File(updatesDir, "mcbd_update.apk")
        if (targetFile.exists()) targetFile.delete()

        try {
            val request = Request.Builder().url(downloadUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to download APK: HTTP ${response.code}")
                }
                val body = response.body ?: throw Exception("Empty response from update server")
                val totalBytes = body.contentLength()
                val totalMb = if (totalBytes > 0) totalBytes / (1024f * 1024f) else 19.0f

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(targetFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    val progress = if (totalBytes > 0) (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0.5f
                    val percentage = (progress * 100).toInt()
                    val downloadedMb = totalRead / (1024f * 1024f)
                    emit(DownloadStatus.Downloading(progress, percentage, downloadedMb, totalMb))
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
            }

            emit(DownloadStatus.Completed(targetFile))
        } catch (e: Exception) {
            Log.e("AppUpdateManager", "Real download failed", e)
            emit(DownloadStatus.Error(e.message ?: "Download failed. Please check network connection."))
        }
    }.flowOn(Dispatchers.IO)

    fun installApk(apkFile: File): Boolean {
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "com.mcbdone.app.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e("AppUpdateManager", "Error launching APK installer", e)
            // Fallback: Open browser / direct link
            false
        }
    }
}
