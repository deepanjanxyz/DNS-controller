package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    
    // UI ভেরিয়েবল
    private lateinit var btnVpn: Button
    private lateinit var switchAds: Switch
    private lateinit var switchFamily: Switch
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // আইডিগুলো কানেক্ট করা
        btnVpn = findViewById(R.id.vpnButton)
        switchAds = findViewById(R.id.switchAds)
        switchFamily = findViewById(R.id.switchFamily)
        txtStatus = findViewById(R.id.statusText)

        btnVpn.setOnClickListener {
            val intent = VpnService.prepare(this)
            if (intent != null) {
                startActivityForResult(intent, VPN_REQUEST_CODE)
            } else {
                startService(Intent(this, MyVpnService::class.java))
                updateStatus(true)
            }
        }
    }

    private fun updateStatus(active: Boolean) {
        if (active) {
            txtStatus.text = "SHIELD ACTIVE 🛡️"
            txtStatus.setTextColor(android.graphics.Color.GREEN)
            btnVpn.text = "STOP"
            btnVpn.setBackgroundColor(android.graphics.Color.RED)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startService(Intent(this, MyVpnService::class.java))
            updateStatus(true)
        }
    }
}
