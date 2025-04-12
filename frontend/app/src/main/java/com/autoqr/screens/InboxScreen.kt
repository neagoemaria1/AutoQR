package com.autoqr.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.autoqr.model.InboxMessage
import com.autoqr.services.InboxRepository
import com.autoqr.storage.DataStoreManager
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboxScreenContent(navController: NavController) {
    val context = LocalContext.current
    val inboxRepository = remember { InboxRepository(context) }
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf(emptyList<InboxMessage>()) }
    var currentUsername by remember { mutableStateOf("") }

    val dataStore = remember { DataStoreManager(context) }
    LaunchedEffect(Unit) {
        dataStore.token.collect { token ->
            dataStore.username.collect { username ->
                if (!token.isNullOrBlank() && !username.isNullOrBlank()) {
                    currentUsername = username
                    scope.launch {
                        val fetched = inboxRepository.fetchInboxMessages(token)
                        messages = fetched.sortedByDescending { it.timestamp }
                    }
                }
            }
        }
    }

    val alerts = messages.filter {
        it.messageType == "alert" && it.toUsername == currentUsername
    }
    val replies = messages.filter {
        it.messageType == "reply" && it.toUsername == currentUsername
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (alerts.isNotEmpty()) {
            item {
                Text("\uD83D\uDD14 Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            items(alerts) { msg ->
                MessageItem(msg) {
                    val encodedOriginal = java.net.URLEncoder.encode(msg.message, "UTF-8")
                    navController.navigate("replyMessage/${msg.fromUsername}/$encodedOriginal")
                }

            }
        }

        if (replies.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("\uD83D\uDCAC Replies to Your Messages", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            items(replies) { replyMsg ->
                val original = messages.find {
                    it.message == replyMsg.replyTo &&
                            it.fromUsername == replyMsg.toUsername &&
                            it.toUsername == replyMsg.fromUsername
                }
                val originalBody = original?.message
                ReplyMessageItem(replyMessage = replyMsg, originalMessage = originalBody)
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageItem(
    message: InboxMessage,
    onReply: () -> Unit
) {
    val dateText = remember(message.timestamp) {
        val instant = Instant.ofEpochSecond(message.timestamp)
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = message.fromUsername.firstOrNull()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${message.fromUsername}: ${message.message}",
                        fontSize = 16.sp,
                        fontWeight = if (!message.isRead) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (!message.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("\uD83D\uDD35", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            TextButton(
                onClick = {    onReply() },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Reply",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReplyMessageItem(replyMessage: InboxMessage, originalMessage: String?) {
    val dateText = remember(replyMessage.timestamp) {
        val instant = Instant.ofEpochSecond(replyMessage.timestamp)
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Reply from ${replyMessage.fromUsername}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            if (!originalMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("In reply to:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = originalMessage,
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(replyMessage.message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(8.dp))
            Text(dateText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
