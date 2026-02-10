package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isRunning = false
    
    private lateinit var btnVpn: Button
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVpn = findViewById(R.id.vpnButton)
        txtStatus = findViewById(R.id.statusText)

        btnVpn.setOnClickListener {
            if (!isRunning) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, VPN_REQUEST_CODE)
                } else {
                    startService(Intent(this, MyVpnService::class.java))
                    updateUI(true)
                }
            } else {
                // স্টপ কমান্ড পাঠানো হচ্ছে
                val intent = Intent(this, MyVpnService::class.java)
                intent.action = "STOP"
                startService(intent)
                updateUI(false)
            }
        }
    }

    private fun updateUI(active: Boolean) {
        isRunning = active
        if (active) {
            txtStatus.text = "SHIELD ACTIVE 🛡️"
            txtStatus.setTextColor(android.graphics.Color.GREEN)
            btnVpn.text = "STOP SHIELD"
            btnVpn.setBackgroundColor(android.graphics.Color.RED)
        } else {
            txtStatus.text = "SHIELD OFF"
            txtStatus.setTextColor(android.graphics.Color.WHITE)
            btnVpn.text = "START SHIELD"
            btnVpn.setBackgroundColor(android.graphics.Color.BLUE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startService(Intent(this, MyVpnService::class.java))
            updateUI(true)
        }
    }
}
