package com.deepanjan.dnsblocker
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.deepanjan.dnsblocker.service.DNSVpnService
import com.deepanjan.dnsblocker.ui.MainScreen
import com.deepanjan.dnsblocker.ui.DNSStats
class MainActivity : ComponentActivity() {
    private var isConnected = false
    private var stats = DNSStats("NextDNS", 0, "0s")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(isConnected = isConnected, onToggle = { toggleVpn() }, stats = stats)
        }
    }
    private fun toggleVpn() {
        if (!isConnected) {
            val intent = VpnService.prepare(this)
            if (intent != null) { startActivityForResult(intent, 0) }
            else { startVPN() }
        } else { stopVPN() }
    }
    private fun startVPN() {
        val intent = Intent(this, DNSVpnService::class.java)
        startService(intent); isConnected = true
    }
    private fun stopVPN() {
        val intent = Intent(this, DNSVpnService::class.java)
        stopService(intent); isConnected = false
    }
}
