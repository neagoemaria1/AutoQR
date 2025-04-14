package com.autoqr.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoqr.model.LoginRequest
import com.autoqr.network.ApiClient
import com.autoqr.storage.DataStoreManager
import com.autoqr.ui.Color.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })

    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        color = White
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0D0D0D)
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {

            Box(modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))
                    )
                )
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val topDiag = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.25f)
                    lineTo(0f, h * 0.35f)
                    close()
                }
                drawPath(
                    path = topDiag,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF444444).copy(alpha = 0.25f),
                            Color(0xFF303030).copy(alpha = 0.1f)
                        ),
                        start = Offset.Zero,
                        end = Offset(w, 0f)
                    )
                )
                val bottomDiag = Path().apply {
                    moveTo(0f, h)
                    lineTo(w, h)
                    lineTo(w, h * 0.70f)
                    lineTo(0f, h * 0.60f)
                    close()
                }
                drawPath(
                    path = bottomDiag,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF217373).copy(alpha = 0.2f),
                            Color(0xFF14FFEC).copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, h),
                        end = Offset(w, h * 0.7f)
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF14FFEC).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w * 0.8f, h * 0.25f)
                    ),
                    radius = size.minDimension * 0.4f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Text("Login", style = titleStyle)
                Spacer(modifier = Modifier.height(30.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = White) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = keyboardActions,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .alpha(0.95f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.6f),
                        cursorColor = ElectricBlue,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    )
                )
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = White) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                if (passwordVisible) "👁️" else "👁️‍🗨️",
                                color = White
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = keyboardActions,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
                    visualTransformation =
                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .alpha(0.95f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.6f),
                        cursorColor = ElectricBlue,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    )
                )
                Spacer(modifier = Modifier.height(50.dp))
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Email and password are required.")
                            }
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            try {
                                val api = ApiClient.create(context)
                                val response = api.login(LoginRequest(email, password))
                                if (response.isSuccessful) {
                                    val token = response.body()?.token ?: ""
                                    dataStore.saveToken(token)
                                    val userResponse = api.getCurrentUser("Bearer $token")
                                    if (userResponse.isSuccessful) {
                                        val username = userResponse.body()?.username ?: ""
                                        if (username.isNotBlank()) {
                                            dataStore.saveUsername(username)
                                        }
                                    }
                                    FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val deviceTokenResponse = api.updateDeviceToken(
                                                    token = "Bearer $token",
                                                    deviceToken = fcmToken
                                                )
                                                if (!deviceTokenResponse.isSuccessful) {
                                                    println("Device token update failed: ${deviceTokenResponse.code()}")
                                                }
                                            } catch (e: Exception) {
                                                println("Error updating device token: ${e.message}")
                                            }
                                        }
                                    }
                                    isLoading = false
                                    onLoginSuccess()
                                    snackbarHostState.showSnackbar("Login successful!")
                                } else {
                                    isLoading = false
                                    snackbarHostState.showSnackbar("Login failed: ${response.code()}")
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("LOGIN", color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(18.dp))
                TextButton(onClick = onSwitchToRegister) {
                    Text("Don't have an account? Register here", color = White)
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = ElectricBlue)
                }
            }
        }
    }
}
