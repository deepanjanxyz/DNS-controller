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
    private var isAdultFilterOn = false
    
    private lateinit var btnShield: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var btnAdultFilter: LinearLayout
    private lateinit var statusAdult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShield = findViewById(R.id.btnShieldToggle)
        txtStatus = findViewById(R.id.txtStatus)
        btnAdultFilter = findViewById(R.id.cardFamily)
        statusAdult = findViewById(R.id.statusFamily)

        btnShield.setOnClickListener {
            if (!isShieldOn) {
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpnService()
            } else {
                stopVpnService()
            }
        }

        btnAdultFilter.setOnClickListener {
            isAdultFilterOn = !isAdultFilterOn
            if (isAdultFilterOn) {
                statusAdult.text = "ON"
                statusAdult.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
            } else {
                statusAdult.text = "OFF"
                statusAdult.setTextColor(ContextCompat.getColor(this, R.color.neon_red))
            }
            // রিস্টার্ট করার দরকার নেই, পরের বার কানেক্ট করলেই নতুন ডিএনএস পাবে
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
            btnShield.setColorFilter(ContextCompat.getColor(this, R.color.neon_green))
            txtStatus.text = "SHIELD PROTECTED"
            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
            
            val pulse = AlphaAnimation(0.5f, 1.0f)
            pulse.duration = 1000
            pulse.repeatMode = Animation.REVERSE
            pulse.repeatCount = Animation.INFINITE
            btnShield.startAnimation(pulse)
        } else {
            btnShield.setColorFilter(ContextCompat.getColor(this, R.color.neon_blue))
            txtStatus.text = "TAP TO CONNECT"
            txtStatus.setTextColor(android.graphics.Color.GRAY)
            btnShield.clearAnimation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) startVpnService()
    }
}
