package com.deepanjan.dnsblocker.service
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.*
import java.net.DatagramChannel
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class DNSVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        setupVPN()
        return START_STICKY
    }
    private fun setupVPN() {
        val builder = Builder()
        builder.setSession("DNSBlocker")
            .addAddress("10.0.0.2", 32)
            .addDnsServer("45.90.28.0")
            .addRoute("0.0.0.0", 0)
        vpnInterface = builder.establish()
        scope.launch { interceptDNS() }
    }
    private suspend fun interceptDNS() {
        val channel = DatagramChannel.open()
        channel.connect(InetSocketAddress("dns.nextdns.io", 53))
        val buffer = ByteBuffer.allocate(1024)
        while (isActive) {
            buffer.clear()
            val len = channel.read(buffer)
        }
    }
    override fun onDestroy() {
        vpnInterface?.close()
        scope.cancel()
        super.onDestroy()
    }
}
