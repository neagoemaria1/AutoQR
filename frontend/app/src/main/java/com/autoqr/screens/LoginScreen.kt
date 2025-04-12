package com.autoqr.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoqr.model.LoginRequest
import com.autoqr.network.ApiClient
import com.autoqr.storage.DataStoreManager
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
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    val inputFieldColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Login", style = titleStyle)
            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = keyboardActions,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = inputFieldColor,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            if (passwordVisible) "👁️" else "👁️‍🗨️",
                            color = textColor
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = keyboardActions,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = inputFieldColor,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // LOGIN Button
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

                                // 🔄 Obține username-ul cu /api/auth/me
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
                                                println("⚠️ Device token update failed: ${deviceTokenResponse.code()}")
                                            }
                                        } catch (e: Exception) {
                                            println("⚠️ Error updating device token: ${e.message}")
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
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Login", color = ComposeColor.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { onSwitchToRegister() }) {
                Text("Don't have an account? Register here")
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}
