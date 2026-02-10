package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isRunning = false
    
    private lateinit var btnVpn: Button
    private lateinit var btnDownload: Button
    private lateinit var txtStatus: TextView
    private lateinit var txtFilters: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVpn = findViewById(R.id.vpnButton)
        btnDownload = findViewById(R.id.downloadButton)
        txtStatus = findViewById(R.id.statusText)
        txtFilters = findViewById(R.id.filterStats)

        // হ্যাকার লিস্ট ডাউনলোড বাটন
        btnDownload.setOnClickListener {
            txtFilters.text = "Downloading Hacker Database..."
            btnDownload.isEnabled = false
            
            FilterManager.downloadFilterList(this) { success, count ->
                runOnUiThread {
                    btnDownload.isEnabled = true
                    if (success) {
                        txtFilters.text = "Active Filters: $count Rules"
                        Toast.makeText(this, "List Updated Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        txtFilters.text = "Download Failed!"
                        Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // VPN কানেক্ট বাটন
        btnVpn.setOnClickListener {
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
        txtStatus.text = "SHIELD ACTIVE 🛡️"
        txtStatus.setTextColor(android.graphics.Color.GREEN)
        btnVpn.text = "DISCONNECT"
        btnVpn.setBackgroundColor(android.graphics.Color.RED)
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent)
        isRunning = false
        txtStatus.text = "PROTECTION: OFF"
        txtStatus.setTextColor(android.graphics.Color.RED)
        btnVpn.text = "CONNECT VPN"
        btnVpn.setBackgroundColor(android.graphics.Color.BLUE)
    }
}
