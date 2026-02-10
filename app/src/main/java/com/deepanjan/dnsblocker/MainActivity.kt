package com.deepanjan.dnsblocker

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isShieldOn = false
    private var isAdultFilterOn = false
    
    // UI Elements
    private lateinit var btnShield: ImageView
    private lateinit var shieldGlow: View
    private lateinit var txtStatus: TextView
    private lateinit var cardAds: View
    private lateinit var cardFamily: View
    private lateinit var statusFamily: TextView
    private lateinit var btnSettings: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find Views
        btnShield = findViewById(R.id.btnShieldToggle)
        shieldGlow = findViewById(R.id.shieldGlow)
        txtStatus = findViewById(R.id.txtStatus)
        cardAds = findViewById(R.id.cardAds)
        cardFamily = findViewById(R.id.cardFamily)
        statusFamily = findViewById(R.id.statusFamily)
        btnSettings = findViewById(R.id.btnSettings)

        // 1. Shield Toggle Logic (Pulsating Effect)
        btnShield.setOnClickListener {
            if (!isShieldOn) {
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpnService()
            } else {
                stopVpnService()
            }
        }

        // 2. Adult Filter Toggle
        cardFamily.setOnClickListener {
            isAdultFilterOn = !isAdultFilterOn
            updateFamilyUI()
            if (isShieldOn) {
                // Restart VPN to apply new filter
                stopVpnService()
                startVpnService()
            }
        }

        // 3. Settings Dialog (Simplified for now)
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun startVpnService() {
        val dns = if (isAdultFilterOn) "94.140.14.15" else "94.140.14.14" // Family vs Default
        val intent = Intent(this, MyVpnService::class.java).putExtra("DNS_IP", dns)
        startService(intent)
        
        isShieldOn = true
        updateShieldUI(true)
        startPulseAnimation()
    }

    private fun stopVpnService() {
        val intent = Intent(this, MyVpnService::class.java).setAction("STOP")
        startService(intent)
        
        isShieldOn = false
        updateShieldUI(false)
        stopPulseAnimation()
    }

    private fun updateShieldUI(active: Boolean) {
        if (active) {
            btnShield.setColorFilter(android.graphics.Color.parseColor("#00E676")) // Green
            txtStatus.text = "PROTECTED"
            txtStatus.setTextColor(android.graphics.Color.GREEN)
        } else {
            btnShield.setColorFilter(android.graphics.Color.parseColor("#00E5FF")) // Blue
            txtStatus.text = "Tap to Connect"
            txtStatus.setTextColor(android.graphics.Color.GRAY)
        }
    }

    private fun updateFamilyUI() {
        if (isAdultFilterOn) {
            statusFamily.text = "ON"
            statusFamily.setTextColor(android.graphics.Color.RED)
        } else {
            statusFamily.text = "OFF"
            statusFamily.setTextColor(android.graphics.Color.GRAY)
        }
    }

    // --- Pulsating Animation Logic ---
    private fun startPulseAnimation() {
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            shieldGlow,
            PropertyValuesHolder.ofFloat("scaleX", 1.5f),
            PropertyValuesHolder.ofFloat("scaleY", 1.5f),
            PropertyValuesHolder.ofFloat("alpha", 0f)
        )
        scaleDown.duration = 1500
        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatMode = ObjectAnimator.RESTART
        scaleDown.start()
    }

    private fun stopPulseAnimation() {
        shieldGlow.animate().cancel()
        shieldGlow.scaleX = 1f
        shieldGlow.scaleY = 1f
        shieldGlow.alpha = 0.3f
    }

    private fun showSettingsDialog() {
        val options = arrayOf("AdGuard DNS (Default)", "Cloudflare Family (1.1.1.3)", "OpenDNS Family")
        AlertDialog.Builder(this)
            .setTitle("Select Upstream DNS")
            .setSingleChoiceItems(options, 0) { dialog, which ->
                Toast.makeText(this, "DNS Updated. Restart Shield.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) startVpnService()
    }
}
