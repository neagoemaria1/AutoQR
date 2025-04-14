package com.autoqr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autoqr.screens.*
import com.autoqr.ui.theme.AutoQRTheme
import com.google.firebase.FirebaseApp
import java.net.URLDecoder

class MainActivity : ComponentActivity() {

    private lateinit var cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private lateinit var notificationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)


        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "Camera permisă: $granted")
        }

        notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "Notificări permise: $granted")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }

        setContent {
            AutoQRTheme {
                AutoQRApp()
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
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
            HomeScreen(navController = navController)
        }

        composable("sendMessage/{qr}") { backStackEntry ->
            val qr = backStackEntry.arguments?.getString("qr") ?: ""
            SendMessageScreen(
                toQrCode = qr,
                prefilledMessage = null,
                navController = navController
            )
        }

        composable("sendMessage/{qr}/{message}") { backStackEntry ->
            val qr = backStackEntry.arguments?.getString("qr") ?: ""
            val message = backStackEntry.arguments?.getString("message") ?: ""
            SendMessageScreen(
                toQrCode = qr,
                prefilledMessage = message,
                navController = navController
            )
        }

        composable("replyMessage/{username}/{original}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: return@composable
            val original = backStackEntry.arguments?.getString("original") ?: return@composable
            ReplyMessageScreen(
                toUsername = username,
                originalMessage = URLDecoder.decode(original, "UTF-8"),
                navController = navController
            )
        }



    }
}
