package com.autoqr.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import java.net.URLEncoder


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboxScreenContent(navController: NavController) {
    val context = LocalContext.current
    val inboxRepository = remember { InboxRepository(context) }
    val scope = rememberCoroutineScope()
    val dataStore = remember { DataStoreManager(context) }
    var token by remember { mutableStateOf("") }
    var currentUsername by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<InboxMessage>>(emptyList()) }

    LaunchedEffect(Unit) {
        dataStore.token.collect { t ->
            dataStore.username.collect { u ->
                if (!t.isNullOrBlank() && !u.isNullOrBlank()) {
                    token = t
                    currentUsername = u
                    scope.launch {
                        val fetched = inboxRepository.fetchInboxMessages(t)
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
                    Brush.verticalGradient(listOf(Color(0xFF050505), Color(0xFF151515)))
                )
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawPath(
                path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.25f)
                    lineTo(0f, h * 0.35f)
                    close()
                },
                brush = Brush.linearGradient(
                    listOf(Color(0xFF444444).copy(alpha = 0.25f), Color(0xFF303030).copy(alpha = 0.1f))
                )
            )
            drawPath(
                path = Path().apply {
                    moveTo(0f, h)
                    lineTo(w, h)
                    lineTo(w, h * 0.70f)
                    lineTo(0f, h * 0.60f)
                    close()
                },
                brush = Brush.linearGradient(
                    listOf(Color(0xFF217373).copy(alpha = 0.2f), Color(0xFF14FFEC).copy(alpha = 0.05f))
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF14FFEC).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.25f),
                    radius = size.minDimension * 0.4f
                )
            )
        }

        if (token.isBlank()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = ElectricBlue)
        } else {
            InboxTabs(
                messages.filter { it.toUsername == currentUsername && it.messageType == "alert" },
                messages.filter { it.toUsername == currentUsername && it.messageType == "reply" },
                navController,
                currentUsername,
                token
            ) { deletedMessage ->
                messages = messages.filterNot { it.id == deletedMessage.id }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboxTabs(
    alerts: List<InboxMessage>,
    replies: List<InboxMessage>,
    navController: NavController,
    currentUsername: String,
    token: String,
    onDeleteMessage: (InboxMessage) -> Unit
) {
    val pagerState = rememberPagerState { 2 }
    val tabTitles = listOf("Alerts", "Replies")
    val scope = rememberCoroutineScope()

    Column {
        TabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.Transparent) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = title,
                            color = if (pagerState.currentPage == index) ElectricBlue else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> AlertTabContent(alerts, navController, token, onDeleteMessage)
                1 -> RepliesTabContent(replies, token, onDeleteMessage)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlertTabContent(
    alerts: List<InboxMessage>,
    navController: NavController,
    token: String,
    onDeleteMessage: (InboxMessage) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { InboxRepository(context) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(alerts) { msg ->
            ModernMessageCard(
                message = msg,
                onReply = {
                    val encoded = URLEncoder.encode(msg.message, "UTF-8")
                    navController.navigate("replyMessage/${msg.fromUsername}/$encoded")
                },
                onDelete = {
                    scope.launch {
                        val deleted = repo.deleteMessage(token, msg.id)
                        if (deleted) onDeleteMessage(msg)
                    }
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RepliesTabContent(
    replies: List<InboxMessage>,
    token: String,
    onDeleteMessage: (InboxMessage) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { InboxRepository(context) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(replies) { reply ->
            ModernReplyCard(
                replyMessage = reply,
                originalMessage = reply.replyTo,
                onDelete = {
                    scope.launch {
                        val deleted = repo.deleteMessage(token, reply.id)
                        if (deleted) onDeleteMessage(reply)
                    }
                }
            )
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
fun ModernMessageCard(
    message: InboxMessage,
    onReply: () -> Unit,
    onDelete: () -> Unit
) {
    val dateText = remember(message.timestamp) {
        val instant = Instant.ofEpochSecond(message.timestamp)
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(10.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {

            Surface(
                modifier = Modifier
                    .size(44.dp)
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.fromUsername,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
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

                Spacer(modifier = Modifier.height(8.dp))

                // Mesajul propriu-zis
                Text(
                    text = message.message,
                    fontSize = 15.sp,
                    color = White,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateText,
                        fontSize = 12.sp,
                        color = MediumGray
                    )

                    Row {
                        IconButton(onClick = onReply) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = "Reply",
                                tint = LimeGreen
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModernReplyCard(
    replyMessage: InboxMessage,
    originalMessage: String?,
    onDelete: () -> Unit
) {
    val dateText = remember(replyMessage.timestamp) {
        val instant = Instant.ofEpochSecond(replyMessage.timestamp)
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(10.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                color = ElectricBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = replyMessage.fromUsername.firstOrNull()?.uppercase() ?: "?",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = replyMessage.fromUsername,
                        color = InfoBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "replied",
                        color = MediumGray,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Original message
                if (!originalMessage.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Original",
                            tint = MediumGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "\"$originalMessage\"",
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = MediumGray
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = Color(0xFF2D2D2D), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Reply text
                Text(
                    text = replyMessage.message,
                    fontSize = 15.sp,
                    color = White,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateText,
                        fontSize = 12.sp,
                        color = MediumGray
                    )

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

