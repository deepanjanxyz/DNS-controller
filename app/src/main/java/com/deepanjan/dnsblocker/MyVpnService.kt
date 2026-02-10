package com.deepanjan.dnsblocker

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.content.Intent
import android.util.Log

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }

        // AdGuard DNS Servers (অ্যাড ব্লক করার জন্য)
        val dnsServers = arrayOf("94.140.14.14", "94.140.15.15")

        try {
            val builder = Builder()
                .setSession("Net Shield")
                .addAddress("10.0.0.2", 32) // লোকাল ভার্চুয়াল অ্যাড্রেস
                .addRoute("0.0.0.0", 0)    // সব ট্রাফিক রুট করা হচ্ছে

            // আসল কাজ এখানে: AdGuard DNS যোগ করা
            for (dns in dnsServers) {
                builder.addDnsServer(dns)
            }

            vpnInterface = builder.establish()
            Log.d("NetShield", "VPN Started with AdGuard DNS")
            
        } catch (e: Exception) {
            Log.e("NetShield", "Error starting VPN", e)
        }

        return START_STICKY
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
