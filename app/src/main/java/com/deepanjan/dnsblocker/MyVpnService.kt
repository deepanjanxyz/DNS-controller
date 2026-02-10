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
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        try {
            if (vpnInterface != null) return
            
            val builder = Builder()
            builder.setSession("Net Shield")
            builder.addAddress("10.0.0.2", 32)
            
            // ১. AdGuard DNS সার্ভারগুলো যোগ করা (অ্যাড ব্লকিংয়ের জন্য)
            builder.addDnsServer("94.140.14.14")
            builder.addDnsServer("94.140.15.15")
            
            // ২. এই লাইনটাই আসল - সব ইন্টারনেট ট্রাফিককে ভিপিএন-এর ভেতর দিয়ে পাঠানো
            builder.addRoute("0.0.0.0", 0) 
            
            // ৩. আইপিভি৬ ডিজেবল করা (অনেক সময় লিক হওয়ার কারণ এটা)
            builder.allowFamily(android.system.OsConstants.AF_INET)
            builder.setBlocking(true)

            vpnInterface = builder.establish()
            Log.d("NetShield", "VPN Shield Activated with Global Route")
            
        } catch (e: Exception) {
            Log.e("NetShield", "Failed: ${e.message}")
        }
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }
}
