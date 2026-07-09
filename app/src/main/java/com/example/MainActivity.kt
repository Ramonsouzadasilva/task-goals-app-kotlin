package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LoginRegisterScreen
import com.example.ui.MainDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BoardViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val boardViewModel: BoardViewModel = viewModel()
            val isDarkTheme by boardViewModel.isDarkTheme.collectAsState()
            val currentUser by boardViewModel.currentUser.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = currentUser != null,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "main_screen_transition"
                    ) { isLoggedIn ->
                        if (isLoggedIn) {
                            MainDashboard(viewModel = boardViewModel)
                        } else {
                            LoginRegisterScreen(viewModel = boardViewModel)
                        }
                    }
                }
            }
        }
    }
}
