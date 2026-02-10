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
            // যদি কোনো কারণে আইপি না আসে, তবে ডিফল্ট AdGuard ব্যবহার হবে
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
            
            // সিলেক্ট করা ডিএনএস বসানো হচ্ছে
            builder.addDnsServer(dnsIp)
            
            // রুট সেট করা (DNS-Only Mode)
            // এটা না দিলে অনেক ফোনেই ভিপিএন কানেক্ট দেখায় কিন্তু কাজ করে না
            builder.addRoute(dnsIp, 32) 
            
            builder.setBlocking(true)
            builder.setSession("ABS Shield: $dnsIp")
            
            vpnInterface = builder.establish()
            Log.d("VPN", "VPN Started with DNS: $dnsIp")
            
        } catch (e: Exception) {
            Log.e("VPN", "Error starting VPN: ${e.message}")
            e.printStackTrace()
            stopSelf() // এরর হলে সার্ভিস বন্ধ করে দেবে
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            stopSelf()
            Log.d("VPN", "VPN Stopped")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
