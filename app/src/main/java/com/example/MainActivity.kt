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
import com.example.ui.screens.LoginRegisterScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val taskViewModel: TaskViewModel = viewModel()
            val isDarkTheme by taskViewModel.isDarkTheme.collectAsState()
            val currentUsername by taskViewModel.currentUsername.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = currentUsername != null,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "main_screen_transition"
                    ) { isLoggedIn ->
                        if (isLoggedIn) {
                            MainDashboardScreen(
                                viewModel = taskViewModel,
                                onLogout = {}
                            )
                        } else {
                            LoginRegisterScreen(
                                viewModel = taskViewModel,
                                onLoginSuccess = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
