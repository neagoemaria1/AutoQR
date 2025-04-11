// File: SendMessageScreen.kt
package com.autoqr.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoqr.viewmodel.MessageViewModel

@Composable
fun SendMessageScreen(
    token: String,
    fromUsername: String,
    toQrCode: String,
    onMessageSent: () -> Unit
) {
    val messageViewModel: MessageViewModel = viewModel()

    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var isMessageSent by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val sendMessage = {
        // Apelăm ViewModel-ul pentru trimiterea mesajului către backend
        messageViewModel.sendMessage(token, fromUsername, toQrCode, messageText.text) { success ->
            isMessageSent = success
            isError = !success
            if (success) {
                onMessageSent()
            }
        }
    }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Send Message", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(16.dp)
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.small),
                singleLine = false,
                decorationBox = { innerTextField ->
                    if (messageText.text.isEmpty()) {
                        Text("Type your message here...", color = Color.Gray)
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = sendMessage,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Send", color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isMessageSent) {
                Text("Message sent successfully!", color = Color.Green)
            } else if (isError) {
                Text("Failed to send message.", color = Color.Red)
            }
        }
    }
}
