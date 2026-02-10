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
        if (vpnInterface != null) return // Already running
        try {
            val builder = Builder()
            
            // ১. ভার্চুয়াল আইপি সেট করা
            builder.addAddress("10.0.0.2", 32)
            
            // ২. সব ট্রাফিক ভিপিএন-এ ঘোরানো (আপাতত টেস্টের জন্য)
            builder.addRoute("0.0.0.0", 0)
            
            // ৩. কানেকশন নাম
            builder.setSession("DNS Blocker Pro")
            
            // ৪. ইন্টারফেস তৈরি
            vpnInterface = builder.establish()
            Log.d("VPN", "VPN Interface Created")
            
        } catch (e: Exception) {
            e.printStackTrace()
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

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
