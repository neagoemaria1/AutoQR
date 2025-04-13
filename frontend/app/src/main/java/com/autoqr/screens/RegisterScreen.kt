package com.autoqr.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoqr.model.RegisterRequest
import com.autoqr.network.ApiClient
import com.autoqr.ui.Color.ElectricBlue
import com.autoqr.ui.Color.White
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0D0D0D)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
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
                Text(
                    text = "Create Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(30.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = White) },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = ElectricBlue)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
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
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = White) },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = ElectricBlue)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
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
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = ElectricBlue)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "👁️" else "👁️‍🗨️", color = White)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = keyboardActions,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                        if (email.isBlank() || username.isBlank() || password.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please fill in all fields.") }
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            try {
                                val api = ApiClient.create(context)
                                val response = api.register(RegisterRequest(email, password, username))
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
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("REGISTER", color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (isLoading) CircularProgressIndicator(color = ElectricBlue)
                qrBitmap?.let {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Your QR Code", fontSize = 18.sp, color = White, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code")
                }
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = onSwitchToLogin) {
                    Text("Already have an account? Login here", color = ElectricBlue)
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
