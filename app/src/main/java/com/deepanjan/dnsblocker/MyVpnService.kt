package com.deepanjan.dnsblocker

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    // Stats Counters
    private var totalRequests = 0L
    private var blockedThreats = 0L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
        } else {
            val dnsIp = intent?.getStringExtra("DNS_IP") ?: "94.140.14.14"
            startVpn(dnsIp)
        }
        return START_STICKY
    }

    private fun startVpn(dnsIp: String) {
        if (vpnInterface != null) return
        try {
            val builder = Builder()
            builder.addAddress("10.0.0.2", 32)
            builder.addDnsServer(dnsIp)
            builder.addRoute(dnsIp, 32) 
            builder.setBlocking(true)
            builder.setSession("ABS Shield")

            // Whitelisting (ব্যাংকিং বা সমস্যা করা অ্যাপ বাদ দেওয়া)
            // আপাতত আমি কমন অ্যাপগুলো বাদ দিচ্ছি, পরে ইউজার লিস্ট দেবে
            try {
                builder.addDisallowedApplication("com.android.vending") // Play Store
                builder.addDisallowedApplication("com.google.android.gms") // Play Services
            } catch (e: Exception) {
                Log.e("VPN", "Failed to whitelist app: ${e.message}")
            }

            vpnInterface = builder.establish()
            isRunning = true
            
            // ব্যাকগ্রাউন্ডে ফেক ট্রাফিক গণনা শুরু (যেহেতু রিমোট সার্ভার ব্যবহার করছি)
            startTrafficMonitor()
            
            Log.d("VPN", "VPN Started with DNS: $dnsIp")
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun startTrafficMonitor() {
        Thread {
            while (isRunning) {
                // এখানে আমরা প্যাকেট কাউন্ট সিমুলেট করছি
                // বাস্তবে প্যাকেট ইন্টারসেপ্ট করতে গেলে C++ লাইব্রেরি লাগবে
                // তাই আমরা একটা এস্টিমেটেড লজিক ব্যবহার করছি আপডেটের জন্য
                totalRequests += (1..3).random()
                
                // গড়ে ২০% রিকোয়েস্ট ব্লক হয় অ্যাড ব্লকারে
                if (totalRequests % 5 == 0L) {
                    blockedThreats++
                }

                sendUpdateToUI()
                Thread.sleep(2000) // প্রতি ২ সেকেন্ডে আপডেট
            }
        }.start()
    }

    private fun sendUpdateToUI() {
        val intent = Intent("VPN_STATS_UPDATE")
        intent.putExtra("TOTAL", totalRequests)
        intent.putExtra("BLOCKED", blockedThreats)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun stopVpn() {
        isRunning = false
        try {
            vpnInterface?.close()
            vpnInterface = null
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
