package com.deepanjan.dnsblocker.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deepanjan.dnsblocker.service.DNSVpnService
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, DNSVpnService::class.java)
            context.startService(serviceIntent)
        }
    }
}
