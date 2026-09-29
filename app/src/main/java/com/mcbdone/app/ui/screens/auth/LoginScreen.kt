package com.mcbdone.app.ui.screens.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.mcbdone.app.data.remote.GoogleAuthManager
import com.mcbdone.app.data.repository.ChatRepository
import com.mcbdone.app.ui.components.GlassButton
import com.mcbdone.app.ui.components.MinecraftAvatar
import com.mcbdone.app.ui.components.ResponsiveScreenContainer
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    chatRepository: ChatRepository,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleAuthManager = remember { GoogleAuthManager(context) }
    var isLoading by remember { mutableStateOf(false) }

    val legacyGoogleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrEmpty()) {
                scope.launch {
                    val loginRes = chatRepository.onGoogleLoginSuccess(idToken)
                    isLoading = false
                    if (loginRes.isSuccess) {
                        Toast.makeText(context, "স্বাগতম MCBD কমিউনিটিতে!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        val err = loginRes.exceptionOrNull()?.message ?: "Supabase Google Provider error"
                        Toast.makeText(context, "Supabase Auth Error: $err", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                isLoading = false
                Toast.makeText(context, "Google Token পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            isLoading = false
            Log.e("LoginScreen", "Google Play Services sign-in error (code: ${e.statusCode})", e)
            val msg = when (e.statusCode) {
                10 -> "Developer Error: Web Client ID বা SHA-1 অমিল"
                12500 -> "Google Play Services Authentication ত্রুটি"
                7 -> "Network error: ইন্টারনেট সংযোগ চেক করুন"
                12501 -> "Sign-in বাতিল করা হয়েছে"
                else -> "Google Sign-In Error (Code: ${e.statusCode})"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    AmbientGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Frosted Glass Hero Card
            Box(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color(0x1810B981))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xE6FFFFFF))
                    .border(1.5.dp, GlassBorderGradient, RoundedCornerShape(32.dp))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating Minecraft Head Avatar with Emerald Glow
                    MinecraftAvatar(
                        avatarUrl = "https://crafthead.net/helm/Steve",
                        ign = "MCBD",
                        size = 80.dp,
                        borderGlow = true,
                        showOnlineBadge = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bangladesh Minecraft Pill Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x1810B981))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MINECRAFT BANGLADESH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MCBD ONE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        text = "The official messaging & server hub for Bangladeshi Minecrafters",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                    )

                    // Feature Highlights in Frosted Glass Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x70F1F5F9))
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(18.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(icon = Icons.Default.Groups, value = "Realtime", label = "Community")
                        StatItem(icon = Icons.Default.Diamond, value = "Showcase", label = "Builds Hub")
                        StatItem(icon = Icons.Default.Speed, value = "Low Ping", label = "Servers")
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Google Login Button (Primary User Request: "Use only google Login")
                    GlassButton(
                        text = if (isLoading) "Signing in with Google..." else "Sign in with Google",
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                isLoading = true
                                scope.launch {
                                    val tokenResult = googleAuthManager.signInWithGoogle(activity)
                                    if (tokenResult.isSuccess) {
                                        val idToken = tokenResult.getOrThrow()
                                        val loginRes = chatRepository.onGoogleLoginSuccess(idToken)
                                        isLoading = false
                                        if (loginRes.isSuccess) {
                                            Toast.makeText(context, "স্বাগতম MCBD কমিউনিটিতে!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        } else {
                                            val err = loginRes.exceptionOrNull()?.message ?: "Supabase Google Provider error"
                                            Toast.makeText(
                                                context,
                                                "Supabase Auth Error: $err",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        Log.w("LoginScreen", "CredentialManager failed: ${tokenResult.exceptionOrNull()?.message}, launching Play Services fallback...")
                                        val client = googleAuthManager.getLegacyGoogleSignInClient(activity)
                                        client.signOut().addOnCompleteListener {
                                            legacyGoogleSignInLauncher.launch(client.signInIntent)
                                        }
                                    }
                                }
                            }
                        },
                        isLoading = isLoading,
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer note
            Text(
                text = "Protected by Supabase & Google Cloud Auth • MCBD 2026",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}
