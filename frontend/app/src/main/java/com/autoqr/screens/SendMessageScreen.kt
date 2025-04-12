package com.autoqr.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoqr.model.SendMessageRequest
import com.autoqr.network.ApiClient
import com.autoqr.storage.DataStoreManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
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

    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = {
            Text("Send Alert", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val username = toQrCode.split(":")[1]
                Text("To: $username", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Message:", fontSize = 14.sp)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = "\"$decodedMessage\"",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 16.sp
                    )
                }

                if (statusText.isNotBlank()) {
                    Text(
                        text = statusText,
                        color = if (statusText.startsWith("✅")) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
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
                                kotlinx.coroutines.delay(1000)
                                navController.popBackStack()
                            }
                        } else {
                            statusText = "❌ Missing token or username"
                            isSending = false
                        }
                    }
                },
                enabled = !isSending
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancel")
            }
        },
        modifier = Modifier.padding(8.dp)
    )
}
