package com.autoqr.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.autoqr.R
import com.autoqr.network.ApiClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val prefs = getSharedPreferences("autoqr", MODE_PRIVATE)
        val jwt = prefs.getString("jwt_token", "") ?: ""
        if (jwt.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ApiClient.create(applicationContext).updateDeviceToken("Bearer $jwt", token)
                } catch (_: Exception) { }
            }
        }
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "FCM"
        val body = remoteMessage.notification?.body ?: "Message"
        val channelId = "fcm_default_channel"
        val id = System.currentTimeMillis().toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "FCM Channel", NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        val n = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(id, n)
        MessagesRepository.saveMessage("$title\n$body")
    }
}
