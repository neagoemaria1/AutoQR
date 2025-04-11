package com.autoqr.screens

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoqr.model.RegisterRequest
import com.autoqr.network.ApiClient
import com.autoqr.utils.generateQrCode
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onSwitchToLogin: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })

    val backgroundColor = MaterialTheme.colorScheme.background
    val inputFieldColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface
    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", style = titleStyle)

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                textColor = textColor,
                inputFieldColor = inputFieldColor,
                keyboardActions = keyboardActions
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                icon = Icons.Filled.Person,
                keyboardType = KeyboardType.Text,
                textColor = textColor,
                inputFieldColor = inputFieldColor,
                keyboardActions = keyboardActions
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordField(
                value = password,
                onValueChange = { password = it },
                visible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                textColor = textColor,
                inputFieldColor = inputFieldColor,
                keyboardActions = keyboardActions
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank() || username.isBlank() || password.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill in all fields.")
                        }
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val api = ApiClient.create(context)
                            val response = api.register(
                                RegisterRequest(email, password, username)
                            )


                            isLoading = false

                            if (response.isSuccessful) {
                                val qr = generateQrCode("autoqr:$username")
                                qrBitmap = qr
                                snackbarHostState.showSnackbar("Account successfully created!")
                            } else {
                                snackbarHostState.showSnackbar("Register failed: ${response.code()}")
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Register", color = ComposeColor.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) CircularProgressIndicator()

            qrBitmap?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your QR Code", style = MaterialTheme.typography.titleMedium.copy(color = textColor))
                Spacer(modifier = Modifier.height(8.dp))
                Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code")
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { onSwitchToLogin() }) {
                Text(
                    "Already have an account? Login here",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// ---------- Components ----------
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    textColor: ComposeColor,
    inputFieldColor: ComposeColor,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = textColor) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        keyboardActions = keyboardActions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = inputFieldColor,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    textColor: ComposeColor,
    inputFieldColor: ComposeColor,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password", color = textColor) },
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Lock, contentDescription = "Password", tint = MaterialTheme.colorScheme.primary)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = keyboardActions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Text(if (visible) "👁️" else "👁️‍🗨️", color = textColor)
            }
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = inputFieldColor,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

