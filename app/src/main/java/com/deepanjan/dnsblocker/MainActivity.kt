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
    private lateinit var radioGroup: RadioGroup
    private lateinit var etCustomDns: EditText
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVpn = findViewById(R.id.vpnButton)
        radioGroup = findViewById(R.id.modeGroup)
        etCustomDns = findViewById(R.id.etCustomDns)
        txtStatus = findViewById(R.id.statusText)

        // মোড পাল্টালে ইনপুট বক্স হাইড/শো হবে
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            etCustomDns.visibility = if (checkedId == R.id.modeCustom) android.view.View.VISIBLE else android.view.View.GONE
        }

        btnVpn.setOnClickListener {
            if (!isRunning) startSelectedMode() else stopVpn()
        }
    }

    private fun startSelectedMode() {
        val selectedDns = when (radioGroup.checkedRadioButtonId) {
            R.id.modeAdBlock -> "94.140.14.14" // AdGuard
            R.id.modeFamily -> "1.1.1.3"      // Cloudflare Family
            else -> etCustomDns.text.toString().trim() // Custom
        }

        if (selectedDns.isEmpty()) {
            Toast.makeText(this, "Enter DNS IP first!", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = VpnService.prepare(this)
        if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
        else {
            val vpnIntent = Intent(this, MyVpnService::class.java)
            vpnIntent.putExtra("DNS_IP", selectedDns)
            startService(vpnIntent)
            updateUI(true)
        }
    }

    private fun stopVpn() {
        startService(Intent(this, MyVpnService::class.java).apply { action = "STOP" })
        updateUI(false)
    }

    private fun updateUI(running: Boolean) {
        isRunning = running
        txtStatus.text = if (running) "SHIELD ACTIVE" else "READY"
        txtStatus.setTextColor(if (running) android.graphics.Color.GREEN else android.graphics.Color.WHITE)
        btnVpn.text = if (running) "STOP" else "START"
    }
}
