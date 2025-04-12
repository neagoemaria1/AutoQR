package com.autoqr.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoqr.model.SendMessageRequest
import com.autoqr.network.ApiClient
import com.autoqr.services.PredefinedMessagesRepository
import com.autoqr.storage.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReplyMessageScreen(
    toUsername: String, // doar username, nu QR complet
    originalMessage: String,
    navController: NavController
) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()
    var selectedReply by remember { mutableStateOf<String?>(null) }
    var replyOptions by remember { mutableStateOf(listOf<String>()) }
    var isSending by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val predefined = PredefinedMessagesRepository(context).loadPredefinedMessages()
        replyOptions = predefined?.replies ?: emptyList()
    }

    val toQrCode = remember(toUsername) { "autoqr:$toUsername" }

    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Send Reply", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("To: $toUsername", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Original message:", fontSize = 14.sp)
                Text(
                    text = originalMessage,
                    modifier = Modifier.padding(8.dp),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 15.sp
                )

                Text("Choose reply:", fontWeight = FontWeight.Medium)

                replyOptions.forEach { reply ->
                    Text(
                        text = reply,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selectedReply = reply },
                        color = if (reply == selectedReply) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                enabled = !isSending && selectedReply != null,
                onClick = {
                    isSending = true
                    scope.launch {
                        val token = dataStore.token.first().orEmpty()
                        val fromUsername = dataStore.username.first().orEmpty()
                        val api = ApiClient.create(context)

                        val request = SendMessageRequest(
                            fromUsername = fromUsername,
                            toQrCode = toQrCode, // format auto: autoqr:test
                            body = selectedReply!!,
                            messageType = "reply",
                            replyTo = originalMessage
                        )

                        val response = api.sendMessageToUser("Bearer $token", request)

                        statusText = if (response.isSuccessful) {
                            "✅ Reply sent!"
                        } else {
                            "❌ Failed: ${response.code()} ${response.message()}"
                        }

                        isSending = false
                        if (response.isSuccessful) {
                            kotlinx.coroutines.delay(1000)
                            navController.popBackStack()
                        }
                    }
                }
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
