package com.nextvpn.adsblocker.vpn
import android.net.VpnService
import android.content.Intent
import android.os.ParcelFileDescriptor
import java.net.DatagramSocket

class DNSVpnService : VpnService() {
    // তোর দেওয়া সেই মাস্টার কোড এখানে সেট হবে
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
