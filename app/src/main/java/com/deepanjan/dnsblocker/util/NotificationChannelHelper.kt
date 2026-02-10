package com.deepanjan.dnsblocker.util
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
object NotificationChannelHelper {
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("dns_blocker_channel", "DNS Blocker", NotificationManager.IMPORTANCE_LOW)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
