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
            
            // ইউজার যে DNS IP দেবে সেটাই এখানে সেট হবে
            builder.addDnsServer(dnsIp)
            
            builder.setBlocking(true)
            builder.setSession("ABS Custom Shield")
            vpnInterface = builder.establish()
            Log.d("VPN", "Connected to Custom DNS: $dnsIp")
        } catch (e: Exception) {
            Log.e("VPN", "Failed: ${e.message}")
        }
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }
}
