package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isRunning = false
    private lateinit var btn: Button
    private lateinit var txt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.vpnButton)
        txt = findViewById(R.id.statusText)

        btn.setOnClickListener {
            if (!isRunning) {
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpn()
            } else {
                stopVpn()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startVpn()
        }
    }

    private fun startVpn() {
        startService(Intent(this, MyVpnService::class.java))
        isRunning = true
        txt.text = "VPN CONNECTED 🛡️"
        txt.setTextColor(android.graphics.Color.GREEN)
        btn.text = "DISCONNECT"
        btn.setBackgroundColor(android.graphics.Color.RED)
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent)
        isRunning = false
        txt.text = "VPN DISCONNECTED"
        txt.setTextColor(android.graphics.Color.RED)
        btn.text = "CONNECT"
        btn.setBackgroundColor(android.graphics.Color.BLUE)
    }
}
