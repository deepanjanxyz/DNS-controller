package com.deepanjan.dnsblocker

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
        } else {
            startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return
        try {
            val builder = Builder()
            // আমরা এখন শুধু DNS সার্ভার সেট করছি, পুরো ট্রাফিক আটকাচ্ছি না
            // যাতে ইন্টারনেট নরমাল চলে।
            builder.addAddress("10.0.0.2", 32)
            builder.addDnsServer("8.8.8.8") 
            builder.setSession("ABS Shield Active")
            
            // এই লাইনটা ইন্টারনেট বন্ধ করছিল, এটা এখন কমেন্ট আউট থাক
            // builder.addRoute("0.0.0.0", 0) 
            
            vpnInterface = builder.establish()
            Log.d("VPN", "VPN Started in Safe Mode")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
