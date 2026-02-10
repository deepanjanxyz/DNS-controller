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
    private var isVpnRunning = false
    
    private lateinit var statusText: TextView
    private lateinit var vpnButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        vpnButton = findViewById(R.id.vpnButton)

        vpnButton.setOnClickListener {
            if (!isVpnRunning) {
                prepareVpn()
            } else {
                stopVpn()
            }
        }
    }

    private fun prepareVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, MyVpnService::class.java)
        startService(intent)
        isVpnRunning = true
        updateUI()
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent) // Send stop command
        isVpnRunning = false
        updateUI()
    }

    private fun updateUI() {
        if (isVpnRunning) {
            statusText.text = "VPN is ON 🛡️"
            statusText.setTextColor(android.graphics.Color.GREEN)
            vpnButton.text = "Stop VPN"
            vpnButton.setBackgroundColor(android.graphics.Color.RED)
        } else {
            statusText.text = "VPN is OFF ❌"
            statusText.setTextColor(android.graphics.Color.RED)
            vpnButton.text = "Start VPN"
            vpnButton.setBackgroundColor(android.graphics.Color.BLUE)
        }
    }
}
