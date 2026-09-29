package com.mcbdone.app.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.mcbdone.app.data.model.AppVersionInfo
import com.mcbdone.app.ui.theme.*

@Composable
fun VersionUpdateDialog(
    currentVersion: String = "1.0.1",
    versionInfo: AppVersionInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isNewUpdate = versionInfo.versionCode > 2 || versionInfo.versionName != currentVersion

    Dialog(onDismissRequest = {
        if (!versionInfo.isMandatory) onDismiss()
    }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = EmeraldPrimary.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xF5FFFFFF))
                .border(1.5.dp, GlassBorderGradient, RoundedCornerShape(32.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Icon Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFE6F9F0),
                                    Color(0xFFD1FAE5)
                                )
                            )
                        )
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isNewUpdate) Icons.Default.NewReleases else Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isNewUpdate) "New Update Available!" else "Version Control System",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Comparison Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1510B981))
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current: v$currentVersion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    if (isNewUpdate) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Latest: v${versionInfo.versionName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Changelog Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x70F1F5F9))
                        .border(1.dp, Color(0x30000000), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "RELEASE NOTES (${versionInfo.releaseDate})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        versionInfo.changelog.forEach { note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
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
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                if (isNewUpdate) {
                    GlassButton(
                        text = "Download & Update Now",
                        icon = Icons.Default.Download,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.downloadUrl))
                            context.startActivity(intent)
                        },
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!versionInfo.isMandatory) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "Remind Me Later",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    GlassButton(
                        text = "Great, Everything is Up to Date!",
                        onClick = onDismiss,
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
