package com.autoqr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.autoqr.screens.*
import com.autoqr.ui.theme.AutoQRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoQRTheme {
                var currentScreen by remember { mutableStateOf("LOGIN") }

                when (currentScreen) {
                    "LOGIN" -> LoginScreen(
                        onLoginSuccess = {

                            currentScreen = "HOME"
                        },
                        onSwitchToRegister = {
                            currentScreen = "REGISTER"
                        }
                    )

                    "REGISTER" -> RegisterScreen(
                        onSwitchToLogin = {
                            currentScreen = "LOGIN"
                        }
                    )

                    // "HOME" ecranul nou cu tab-uri (Inbox, Scan, Settings)
                    "HOME" -> HomeScreen()
                }
            }
        }
    }
}
