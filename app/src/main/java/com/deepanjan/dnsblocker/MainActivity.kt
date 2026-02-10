package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isShieldOn = false
    private var isAdultFilterOn = false
    
    private lateinit var btnShield: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var cardFamily: View
    private lateinit var statusFamily: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShield = findViewById(R.id.btnShieldToggle)
        txtStatus = findViewById(R.id.txtStatus)
        cardFamily = findViewById(R.id.cardFamily)
        statusFamily = findViewById(R.id.statusFamily)

        btnShield.setOnClickListener {
            if (!isShieldOn) {
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpnService()
            } else {
                stopVpnService()
            }
        }

        cardFamily.setOnClickListener {
            isAdultFilterOn = !isAdultFilterOn
            if (isAdultFilterOn) {
                statusFamily.text = "ON"
                statusFamily.setTextColor(android.graphics.Color.GREEN)
                Toast.makeText(this, "Adult Filter Enabled", Toast.LENGTH_SHORT).show()
            } else {
                statusFamily.text = "OFF"
                statusFamily.setTextColor(android.graphics.Color.RED)
            }
            // If running, restart to apply
            if (isShieldOn) {
                stopVpnService()
                startVpnService()
            }
        }
    }

    private fun startVpnService() {
        val dns = if (isAdultFilterOn) "94.140.14.15" else "94.140.14.14"
        val intent = Intent(this, MyVpnService::class.java).putExtra("DNS_IP", dns)
        startService(intent)
        
        isShieldOn = true
        updateUI(true)
    }

    private fun stopVpnService() {
        val intent = Intent(this, MyVpnService::class.java).setAction("STOP")
        startService(intent)
        
        isShieldOn = false
        updateUI(false)
    }

    private fun updateUI(active: Boolean) {
        if (active) {
            btnShield.setColorFilter(android.graphics.Color.GREEN)
            txtStatus.text = "SHIELD ACTIVE"
            txtStatus.setTextColor(android.graphics.Color.GREEN)
        } else {
            btnShield.setColorFilter(android.graphics.Color.parseColor("#00E5FF"))
            txtStatus.text = "TAP TO CONNECT"
            txtStatus.setTextColor(android.graphics.Color.GRAY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) startVpnService()
    }
}
