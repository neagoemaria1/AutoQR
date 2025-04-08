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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onSwitchToLogin: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardActions = KeyboardActions(
        onDone = { keyboardController?.hide() },
        onNext = {  }
    )

    val backgroundColor = MaterialTheme.colorScheme.background
    val inputFieldColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface


    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(padding)
                .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", style = titleStyle)

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = textColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                maxLines = 1,
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
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = inputFieldColor,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Username Input Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = textColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Username Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
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
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = inputFieldColor,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = textColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Password Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = keyboardActions,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "👁️" else "👁️‍🗨️",
                            style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = inputFieldColor,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = inputFieldColor,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buton de Register
            Button(
                onClick = {
                    if (email.isBlank() || username.isBlank() || password.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill in all fields.")
                        }
                        return@Button
                    }

                    isLoading = true
                    firestore.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener { result ->
                            if (!result.isEmpty) {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Username already exists!")
                                }
                            } else {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { authResult ->
                                        val userId = authResult.user?.uid ?: run {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("User ID is null.")
                                            }
                                            isLoading = false
                                            return@addOnSuccessListener
                                        }

                                        val qrContent = "autoqr:$username"
                                        val qr = generateQrCode(qrContent)
                                        qrBitmap = qr

                                        val userData = hashMapOf(
                                            "username" to username,
                                            "email" to email,
                                            "qrCode" to qrContent
                                        )

                                        firestore.collection("users")
                                            .document(userId)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Account successfully created!")
                                                }
                                            }
                                            .addOnFailureListener {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Failed to save user: ${it.message}")
                                                }
                                            }
                                            .addOnCompleteListener {
                                                isLoading = false
                                            }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Registration failed: ${it.message}")
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Firestore error: ${it.message}")
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

            if (isLoading) {
                CircularProgressIndicator()
            }

            qrBitmap?.let {
                Text(
                    "Your QR Code",
                    style = MaterialTheme.typography.titleMedium.copy(color = textColor)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { onSwitchToLogin() }) {
                Text(
                    "Already have an account? Login here",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// Generare QR, la fel
fun generateQrCode(data: String): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
    for (x in 0 until 512) {
        for (y in 0 until 512) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}
