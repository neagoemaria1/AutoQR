package com.autoqr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autoqr.screens.*
import com.autoqr.ui.theme.AutoQRTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            AutoQRTheme {
                AutoQRApp()
            }
        }
    }
}

@Composable
fun AutoQRApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onSwitchToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(onSwitchToLogin = { navController.popBackStack() })
        }
        composable("home") {
            HomeScreen(navController)
        }
    }
}