package com.autoqr.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoqr.model.SendMessageRequest
import com.autoqr.network.ApiClient
import com.autoqr.storage.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLDecoder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SendMessageScreen(
    toQrCode: String,
    prefilledMessage: String?,
    navController: NavController
) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()
    val decodedMessage = remember(prefilledMessage) {
        URLDecoder.decode(prefilledMessage ?: "", "UTF-8")
    }

    var statusText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    if (decodedMessage.isBlank()) {
        navController.popBackStack()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF050505), Color(0xFF151515))
                )
            )
    ) {
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
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B1B1B))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Send Alert",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00C6FF)
            )

            val username = toQrCode.substringAfter(":")
            Text(
                text = "To: $username",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Text(
                text = "Message:",
                fontSize = 15.sp,
                color = Color.LightGray
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A))
                    .padding(12.dp)
            ) {
                Text(
                    text = "\"$decodedMessage\"",
                    fontSize = 17.sp,
                    color = Color.White
                )
            }

            if (statusText.isNotBlank()) {
                Text(
                    text = statusText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (statusText.startsWith("✅")) Color(0xFF00C6FF) else MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel", fontSize = 16.sp, color = Color.LightGray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        isSending = true
                        scope.launch {
                            val token = dataStore.token.first().orEmpty()
                            val fromUsername = dataStore.username.first().orEmpty()
                            if (token.isNotBlank() && fromUsername.isNotBlank()) {
                                val api = ApiClient.create(context)
                                val request = SendMessageRequest(
                                    fromUsername = fromUsername,
                                    toQrCode = toQrCode,
                                    body = decodedMessage,
                                    messageType = "alert"
                                )
                                val response = api.sendMessageToUser("Bearer $token", request)
                                statusText = if (response.isSuccessful) {
                                    "✅ Message sent!"
                                } else {
                                    "❌ Failed: ${response.code()} ${response.message()}"
                                }
                                isSending = false
                                if (response.isSuccessful) {
                                    delay(1000)
                                    navController.popBackStack()
                                }
                            } else {
                                statusText = "❌ Missing token or username"
                                isSending = false
                            }
                        }
                    },
                    enabled = !isSending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C6FF))
                ) {
                    Text("Send", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}