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
    
    // UI উপাদান (Null Safety সহ)
    private var btnVpn: Button? = null
    private var switchAds: Switch? = null
    private var switchFamily: Switch? = null
    private var switchCustom: Switch? = null
    private var etCustomDns: EditText? = null
    private var txtStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI কানেক্ট করা
        btnVpn = findViewById(R.id.vpnButton)
        switchAds = findViewById(R.id.switchAds)
        switchFamily = findViewById(R.id.switchFamily)
        switchCustom = findViewById(R.id.switchCustom)
        etCustomDns = findViewById(R.id.etCustomDns)
        txtStatus = findViewById(R.id.statusText)

        // স্মার্ট সুইচ লজিক
        switchCustom?.setOnCheckedChangeListener { _, isChecked ->
            etCustomDns?.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) {
                switchAds?.isChecked = false
                switchFamily?.isChecked = false
            }
        }
        
        switchAds?.setOnCheckedChangeListener { _, isChecked -> if(isChecked) switchCustom?.isChecked = false }
        switchFamily?.setOnCheckedChangeListener { _, isChecked -> if(isChecked) switchCustom?.isChecked = false }

        // বাটন ক্লিক
        btnVpn?.setOnClickListener {
            val intent = VpnService.prepare(this)
            if (intent != null) {
                startActivityForResult(intent, VPN_REQUEST_CODE)
            } else {
                startShield()
            }
        }
    }

    private fun startShield() {
        val dns = if (switchCustom?.isChecked == true) etCustomDns?.text.toString() else "94.140.14.14"
        val intent = Intent(this, MyVpnService::class.java)
        intent.putExtra("DNS_IP", dns)
        startService(intent)
        
        txtStatus?.text = "NET SHIELD ACTIVE"
        txtStatus?.setTextColor(android.graphics.Color.GREEN)
        btnVpn?.text = "STOP"
        Toast.makeText(this, "Shield Activated!", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startShield()
        }
    }
}
