package com.deepanjan.dnsblocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val CHANNEL_ID = "NetShieldChannel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        
        // ভিপিএন শুরু করার আগে নোটিফিকেশন দেখানো মাস্ট
        startForeground(1, createNotification())
        startVpn()
        return START_STICKY
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Net Shield Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Net Shield Active")
            .setContentText("Protecting your device from ads & trackers")
            .setSmallIcon(android.R.drawable.ic_lock_lock) // সিস্টেম লক আইকন
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startVpn() {
        try {
            if (vpnInterface != null) return
            
            val builder = Builder()
            builder.setSession("Net Shield")
            builder.addAddress("10.0.0.2", 32)
            
            // AdGuard DNS (অ্যাড ব্লকিং)
            builder.addDnsServer("94.140.14.14")
            builder.addDnsServer("94.140.15.15")

            // নেটওয়ার্ক যাতে স্লো না হয় তার জন্য MTU ফিক্স
            builder.addRoute("0.0.0.0", 0)
            builder.setMtu(1500) 

            vpnInterface = builder.establish()
            Log.d("NetShield", "VPN Started")
            
        } catch (e: Exception) {
            Log.e("NetShield", "Error: ${e.message}")
            stopSelf()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            stopForeground(true)
            stopSelf()
            Log.d("NetShield", "VPN Stopped")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
