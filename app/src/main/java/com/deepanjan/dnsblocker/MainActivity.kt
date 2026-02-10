package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isShieldOn = false
    
    private lateinit var btnShield: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var switchFamily: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShield = findViewById(R.id.btnShieldToggle)
        txtStatus = findViewById(R.id.txtStatus)
        switchFamily = findViewById(R.id.switchFamily)

        btnShield.setOnClickListener {
            if (!isShieldOn) {
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpnService()
            } else {
                stopVpnService()
            }
        }
        
        switchFamily.setOnCheckedChangeListener { _, isChecked ->
            if (isShieldOn) {
                stopVpnService()
                Toast.makeText(this, "Restarting Shield...", Toast.LENGTH_SHORT).show()
                startVpnService()
            }
        }
    }

    private fun startVpnService() {
        val dns = if (switchFamily.isChecked) "94.140.14.15" else "94.140.14.14"
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
            btnShield.setColorFilter(ContextCompat.getColor(this, R.color.neon_green))
            txtStatus.text = "SHIELD ACTIVE"
            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
            
            // Pulsating Animation
            val anim = AlphaAnimation(0.5f, 1.0f)
            anim.duration = 1000
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.INFINITE
            btnShield.startAnimation(anim)
        } else {
            btnShield.setColorFilter(ContextCompat.getColor(this, R.color.neon_blue))
            txtStatus.text = "TAP TO ACTIVATE"
            txtStatus.setTextColor(android.graphics.Color.GRAY)
            btnShield.clearAnimation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) startVpnService()
    }
}
