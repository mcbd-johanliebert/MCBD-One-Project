package com.mcbdone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.mcbdone.app.data.repository.ChatRepository
import com.mcbdone.app.ui.navigation.AppNavigation
import com.mcbdone.app.ui.theme.BgCanvasMid
import com.mcbdone.app.ui.theme.MCBDOneTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var chatRepository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        chatRepository = ChatRepository(this)
        lifecycleScope.launch {
            chatRepository.loadInitialData()
        }

        setContent {
            MCBDOneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgCanvasMid
                ) {
                    AppNavigation(chatRepository = chatRepository)
                }
            }
        }
    }
}
