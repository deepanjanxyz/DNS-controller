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

        // মোড পাল্টালে ইনপুট বক্স দেখাবে কি না
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.modeCustom) {
                etCustomDns.visibility = android.view.View.VISIBLE
            } else {
                etCustomDns.visibility = android.view.View.GONE
            }
        }

        btnVpn.setOnClickListener {
            if (!isRunning) {
                prepareAndStartVpn()
            } else {
                stopVpn()
            }
        }
    }

    private fun prepareAndStartVpn() {
        // ১. আগে চেক করি পারমিশন আছে কি না
        val intent = VpnService.prepare(this)
        if (intent != null) {
            // পারমিশন নেই, তাই চাইছি
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            // পারমিশন অলরেডি আছে, সরাসরি শুরু কর
            startSelectedMode()
        }
    }

    // এই ফাংশনটা পারমিশন পাওয়ার পর কল হবে
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // ফিক্স: আগে এখানে ভুল ছিল, এখন সরাসরি মোড স্টার্ট হবে
            startSelectedMode()
        }
    }

    private fun startSelectedMode() {
        // রেডিও বাটন থেকে আইপি বেছে নেওয়া
        val selectedDns = when (radioGroup.checkedRadioButtonId) {
            R.id.modeAdBlock -> "94.140.14.14" // AdGuard DNS
            R.id.modeFamily -> "1.1.1.3"      // Cloudflare Family
            R.id.modeCustom -> etCustomDns.text.toString().trim()
            else -> "94.140.14.14" // ডিফল্ট
        }

        // যদি কাস্টম মোডে আইপি না দেয়
        if (radioGroup.checkedRadioButtonId == R.id.modeCustom && selectedDns.isEmpty()) {
            Toast.makeText(this, "Please enter a DNS IP", Toast.LENGTH_SHORT).show()
            return
        }

        // সার্ভিস চালু করা
        val vpnIntent = Intent(this, MyVpnService::class.java)
        vpnIntent.putExtra("DNS_IP", selectedDns)
        startService(vpnIntent)
        
        updateUI(true)
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent)
        updateUI(false)
    }

    private fun updateUI(running: Boolean) {
        isRunning = running
        if (running) {
            txtStatus.text = "SHIELD ACTIVE 🛡️"
            txtStatus.setTextColor(android.graphics.Color.GREEN)
            btnVpn.text = "STOP PROTECTION"
            btnVpn.setBackgroundColor(android.graphics.Color.RED)
        } else {
            txtStatus.text = "READY TO CONNECT"
            txtStatus.setTextColor(android.graphics.Color.WHITE)
            btnVpn.text = "START SHIELD"
            btnVpn.setBackgroundColor(android.graphics.Color.BLUE)
        }
    }
}
