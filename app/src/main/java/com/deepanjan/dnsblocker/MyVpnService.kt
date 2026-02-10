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
            
            // ১. লোকাল অ্যাড্রেস
            builder.addAddress("10.0.0.2", 32)
            
            // ২. AdGuard বা Cloudflare এর সিকিউর ডিএনএস ব্যবহার করছি (টেস্টের জন্য)
            // এটা হ্যাকার সাইটগুলোকে অটোমেটিক ব্লক করে
            builder.addDnsServer("94.140.14.14") // AdGuard DNS
            builder.addDnsServer("1.1.1.2")      // Cloudflare Security DNS
            
            // ৩. সব ট্রাফিক ভিপিএন-এর নজরদারিতে আনা
            builder.addRoute("0.0.0.0", 0)
            
            // ৪. সিস্টেমকে বলা যে এই ডিএনএস-ই ফাইনাল
            builder.allowFamily(android.system.OsConstants.AF_INET)
            builder.setBlocking(true)
            
            builder.setSession("ABS Ultra Shield")
            vpnInterface = builder.establish()
            
            Log.d("VPN", "VPN Shield Activated with DNS Force")
        } catch (e: Exception) {
            Log.e("VPN", "Failed to start VPN: ${e.message}")
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
