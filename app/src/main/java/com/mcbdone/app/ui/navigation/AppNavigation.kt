package com.mcbdone.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.mcbdone.app.data.repository.ChatRepository
import com.mcbdone.app.ui.components.GlassBottomBar
import com.mcbdone.app.ui.screens.auth.LoginScreen
import com.mcbdone.app.ui.screens.chat.ChatListScreen
import com.mcbdone.app.ui.screens.chat.ChatRoomScreen
import com.mcbdone.app.ui.screens.profile.ProfileScreen
import com.mcbdone.app.ui.screens.servers.ServerListScreen
import com.mcbdone.app.ui.screens.showcase.BuildsShowcaseScreen

import com.mcbdone.app.ui.components.StrictUpdateOverlay

@Composable
fun AppNavigation(
    chatRepository: ChatRepository,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by chatRepository.currentUser.collectAsState()
    val isStrictUpdateRequired by chatRepository.isStrictUpdateRequired.collectAsState()
    val latestVersion by chatRepository.latestVersion.collectAsState()
    val startDestination = if (currentUser != null) "main" else "login"

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
        composable("login") {
            LoginScreen(
                chatRepository = chatRepository,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainContainer(
                chatRepository = chatRepository,
                onOpenChannel = { channel ->
                    navController.navigate("chatroom")
                },
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        composable("chatroom") {
            val currentChannel by chatRepository.currentChannel.collectAsState()
            ChatRoomScreen(
                channel = currentChannel,
                chatRepository = chatRepository,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    // Strict Blocking In-App Update Overlay
    if (isStrictUpdateRequired) {
        StrictUpdateOverlay(
            currentVersion = chatRepository.currentAppVersion,
            versionInfo = latestVersion,
            modifier = Modifier.fillMaxSize()
        )
    }
}
}

@Composable
fun MainContainer(
    chatRepository: ChatRepository,
    onOpenChannel: (com.mcbdone.app.data.model.Channel) -> Unit,
    onSignOut: () -> Unit
) {
    var currentTab by remember { mutableStateOf("chats") }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentTab) {
            "chats" -> ChatListScreen(
                chatRepository = chatRepository,
                onChannelSelected = onOpenChannel
            )
            "servers" -> ServerListScreen(chatRepository = chatRepository)
            "showcase" -> BuildsShowcaseScreen(chatRepository = chatRepository)
            "profile" -> ProfileScreen(
                chatRepository = chatRepository,
                onSignOut = onSignOut
            )
        }

        // Floating Glass Bottom Bar
        GlassBottomBar(
            currentRoute = currentTab,
            onNavigate = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
