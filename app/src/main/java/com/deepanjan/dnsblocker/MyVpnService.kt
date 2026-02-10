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
            
            // ১. লোকাল আইপি (ভিপিএন ইন্টারফেসের জন্য)
            builder.addAddress("10.0.0.2", 32)
            
            // ২. অ্যাড ব্লকিং ডিএনএস (AdGuard + Cloudflare Family)
            // এগুলো অটোমেটিক অ্যাড এবং ম্যালওয়্যার সাইট ব্লক করে
            builder.addDnsServer("94.140.14.14") 
            builder.addDnsServer("94.140.15.15")
            builder.addDnsServer("1.1.1.2")      
            
            // ৩. স্পিড বাড়ানোর সিক্রেট: আমরা সব রুট (0.0.0.0) অ্যাড করব না
            // বরং ফোনকে ডিএনএস কুয়েরি পাঠাতে বাধ্য করব
            // কোনো রুট অ্যাড না করলে ইন্টারনেট সরাসরি চলবে, শুধু ডিএনএস ফিল্টার হবে
            
            // ৪. সিস্টেমকে ডিএনএস ব্যবহারের জন্য ফোর্স করা
            builder.setBlocking(true)
            builder.setSession("ABS Ultra Shield")
            
            vpnInterface = builder.establish()
            Log.d("VPN", "DNS-Only Shield Activated (Super Fast Mode)")
            
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
