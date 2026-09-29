package com.mcbdone.app.ui.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
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
import com.mcbdone.app.data.model.AppVersionInfo
import com.mcbdone.app.data.remote.AppUpdateManager
import com.mcbdone.app.data.remote.DownloadStatus
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * Strict, Non-Dismissible In-App Update Screen.
 * The user CANNOT exit, dismiss, or use the app until the update is downloaded and installed.
 */
@Composable
fun StrictUpdateOverlay(
    currentVersion: String = "1.0.0",
    versionInfo: AppVersionInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateManager = remember { AppUpdateManager(context) }

    // Intercept hardware and gesture back button strictly!
    BackHandler(enabled = true) {
        Toast.makeText(context, "অনুগ্রহ করে অ্যাপটি আপডেট করুন!", Toast.LENGTH_SHORT).show()
    }

    var downloadStatus by remember { mutableStateOf<DownloadStatus>(DownloadStatus.Idle) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_update")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    AmbientGlassBackground(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(32.dp, RoundedCornerShape(32.dp), spotColor = EmeraldPrimary.copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFAFFFFFF))
                    .border(1.5.dp, GlassBorderGradient, RoundedCornerShape(32.dp))
                    .padding(26.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Critical Update Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MANDATORY UPDATE REQUIRED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFDC2626),
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pulsing Update Crest
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(12.dp, CircleShape, spotColor = EmeraldPrimary.copy(alpha = pulseGlow))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF047857)
                                    )
                                )
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Update Required to Continue",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Version comparison pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x1810B981))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Installed: v$currentVersion",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(12.dp)
                        )
                        Text(
                            text = "Latest: v${versionInfo.versionName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "To ensure server synchronization and continue chatting with Minecraft Bangladesh members, please install the latest update.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // What's new box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x80F1F5F9))
                            .border(1.dp, Color(0x20000000), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "WHAT'S NEW IN V${versionInfo.versionName}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            versionInfo.changelog.forEach { note ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier
                                            .padding(end = 6.dp, top = 2.dp)
                                            .size(12.dp)
                                    )
                                    Text(
                                        text = note,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Dynamic In-App Download Progress & Action Area
                    when (val status = downloadStatus) {
                        is DownloadStatus.Idle -> {
                            GlassButton(
                                text = "Update Now (In-App)",
                                icon = Icons.Default.CloudDownload,
                                onClick = {
                                    scope.launch {
                                        updateManager.downloadAndTrackApk(versionInfo.downloadUrl)
                                            .collect { updateStatus ->
                                                downloadStatus = updateStatus
                                                if (updateStatus is DownloadStatus.Completed) {
                                                    downloadedApkFile = updateStatus.apkFile
                                                    // Auto-trigger package installer!
                                                    updateManager.installApk(updateStatus.apkFile)
                                                }
                                            }
                                    }
                                },
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is DownloadStatus.Downloading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Progress Info Header (Percentage & MBs)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Downloading Update...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${status.percentage}%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Real-Time In-App Progress Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = status.progress)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0xFF34D399),
                                                        Color(0xFF10B981),
                                                        Color(0xFF059669)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Downloaded MB / Total MB indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Speed: High-Speed CDN",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.1f MB / %.1f MB", status.downloadedMb, status.totalMb),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        is DownloadStatus.Completed -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Success Badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Download 100% Complete! Ready to install",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Install Button
                                GlassButton(
                                    text = "Install Update",
                                    icon = Icons.Default.InstallMobile,
                                    onClick = {
                                        val file = downloadedApkFile ?: status.apkFile
                                        updateManager.installApk(file)
                                    },
                                    isPrimary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        is DownloadStatus.Error -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Error: ${status.message}",
                                    fontSize = 12.sp,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                GlassButton(
                                    text = "Retry In-App Download",
                                    icon = Icons.Default.CloudDownload,
                                    onClick = {
                                        scope.launch {
                                            updateManager.downloadAndTrackApk(versionInfo.downloadUrl)
                                                .collect { downloadStatus = it }
                                        }
                                    },
                                    isPrimary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
