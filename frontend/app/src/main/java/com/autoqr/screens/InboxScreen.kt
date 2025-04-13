package com.autoqr.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.autoqr.model.InboxMessage
import com.autoqr.services.InboxRepository
import com.autoqr.storage.DataStoreManager
import com.autoqr.ui.Color.*
import com.autoqr.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.pager.PageSize
import androidx.compose.ui.graphics.drawscope.Fill


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboxScreenContent(navController: NavController) {
    val context = LocalContext.current
    val inboxRepository = remember { InboxRepository(context) }
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<InboxMessage>?>(null) }
    var currentUsername by remember { mutableStateOf("") }

    val dataStore = remember { DataStoreManager(context) }
    LaunchedEffect(Unit) {
        dataStore.token.collect { token ->
            dataStore.username.collect { username ->
                if (!token.isNullOrBlank() && !username.isNullOrBlank()) {
                    currentUsername = username.orEmpty()
                    scope.launch {
                        val fetched = inboxRepository.fetchInboxMessages(token)
                        messages = fetched.sortedByDescending { it.timestamp }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF050505), Color(0xFF151515))
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

        if (messages == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = ElectricBlue
            )
        } else {
            val alerts = messages!!.filter {
                it.messageType == "alert" && it.toUsername == currentUsername
            }
            val replies = messages!!.filter {
                it.messageType == "reply" && it.toUsername == currentUsername
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (alerts.isNotEmpty()) {
                    item {
                        AnimatedSectionTitle("Incoming Alerts", Icons.Default.Notifications, ElectricBlue)
                    }
                    items(alerts) { msg ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut()
                        ) {
                            ModernMessageCard(msg) {
                                val encoded = java.net.URLEncoder.encode(msg.message, "UTF-8")
                                navController.navigate("replyMessage/${msg.fromUsername}/$encoded")
                            }
                        }
                    }
                }

                if (replies.isNotEmpty()) {
                    item {
                        AnimatedSectionTitle("Replies You've Received", Icons.Default.ChatBubble, LimeGreen)
                    }
                    items(replies) { replyMsg ->
                        val original = messages!!.find {
                            it.message == replyMsg.replyTo &&
                                    it.fromUsername == replyMsg.toUsername &&
                                    it.toUsername == replyMsg.fromUsername
                        }
                        val originalBody = original?.let { "${it.fromUsername}: \"${it.message}\"" }
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut()
                        ) {
                            ModernReplyCard(replyMsg, originalBody)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSectionTitle(text: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModernMessageCard(message: InboxMessage, onReply: () -> Unit) {
    val dateText = remember(message.timestamp) {
        val instant = Instant.ofEpochSecond(message.timestamp)
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.2f))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.98f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    color = ElectricBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = message.fromUsername.firstOrNull()?.uppercase() ?: "?",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.fromUsername,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = InfoBlue
                        )
                        if (!message.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NEW",
                                color = SunnyYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(DarkSurface, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message.message,
                        fontSize = 16.sp,
                        color = White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dateText,
                        fontSize = 12.sp,
                        color = MediumGray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onReply) {
                    Icon(Icons.Default.ChatBubble, contentDescription = "Reply", tint = LimeGreen)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModernReplyCard(replyMessage: InboxMessage, originalMessage: String?) {
    val dateText = remember(replyMessage.timestamp) {
        val instant = Instant.ofEpochSecond(replyMessage.timestamp)
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    val (originalAuthor, originalContent) = remember(originalMessage) {
        val parts = originalMessage?.split(": ", limit = 2)
        Pair(parts?.firstOrNull(), parts?.getOrNull(1))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.2f))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.98f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF103D3D)),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Reply from ${replyMessage.fromUsername} to ${replyMessage.replyTo}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = InfoBlue
                )
                if (!originalContent.isNullOrBlank() && !originalAuthor.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(Color(0xFF2B2B2B).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "$originalAuthor said:",
                            fontSize = 13.sp,
                            color = MediumGray,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = originalContent,
                            fontSize = 14.sp,
                            color = LightGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = replyMessage.message,
                    fontSize = 16.sp,
                    color = White,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = MediumGray
                )
            }
        }
    }
}