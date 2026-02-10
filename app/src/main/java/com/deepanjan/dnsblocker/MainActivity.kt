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
    private var isRunning = false
    
    private lateinit var btnVpn: Button
    private lateinit var switchAds: Switch
    private lateinit var switchFamily: Switch
    private lateinit var switchCustom: Switch
    private lateinit var etCustomDns: EditText
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVpn = findViewById(R.id.vpnButton)
        switchAds = findViewById(R.id.switchAds)
        switchFamily = findViewById(R.id.switchFamily)
        switchCustom = findViewById(R.id.switchCustom)
        etCustomDns = findViewById(R.id.etCustomDns)
        txtStatus = findViewById(R.id.statusText)

        // কাস্টম সুইচ অন করলে বাকিগুলো অফ হয়ে যাবে (স্মার্ট ফ্লো)
        switchCustom.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchAds.isChecked = false
                switchFamily.isChecked = false
                etCustomDns.visibility = View.VISIBLE
            } else {
                etCustomDns.visibility = View.GONE
            }
        }

        // অ্যাড বা ফ্যামিলি অন করলে কাস্টম অফ হয়ে যাবে
        val autoDisableCustom = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchCustom.isChecked = false
                etCustomDns.visibility = View.GONE
            }
        }
        switchAds.setOnCheckedChangeListener(autoDisableCustom)
        switchFamily.setOnCheckedChangeListener(autoDisableCustom)

        btnVpn.setOnClickListener {
            if (!isRunning) prepareAndStartVpn() else stopVpn()
        }
    }

    private fun prepareAndStartVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
        else determineDnsAndStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            determineDnsAndStart()
        }
    }

    private fun determineDnsAndStart() {
        // স্মার্ট লজিক: ইউজার কী কী টগল অন করেছে তার ওপর ভিত্তি করে সেরা DNS বেছে নেওয়া
        var dnsToUse = ""
        var modeName = ""

        if (switchCustom.isChecked) {
            dnsToUse = etCustomDns.text.toString().trim()
            modeName = "Custom"
            if (dnsToUse.isEmpty()) {
                Toast.makeText(this, "Enter a valid IP!", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            val ads = switchAds.isChecked
            val family = switchFamily.isChecked

            if (ads && family) {
                dnsToUse = "94.140.14.15" // AdGuard Family (Ads + Adult Block)
                modeName = "Ultra Shield (Ads + Family)"
            } else if (ads) {
                dnsToUse = "94.140.14.14" // AdGuard Default (Only Ads)
                modeName = "Ad-Blocker"
            } else if (family) {
                dnsToUse = "1.1.1.3"      // Cloudflare Family (Only Malware + Adult)
                modeName = "Family Safe"
            } else {
                Toast.makeText(this, "Please select at least one filter!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        startVpnService(dnsToUse, modeName)
    }

    private fun startVpnService(dnsIp: String, mode: String) {
        val intent = Intent(this, MyVpnService::class.java)
        intent.putExtra("DNS_IP", dnsIp)
        startService(intent)
        
        isRunning = true
        txtStatus.text = "ACTIVE: $mode"
        txtStatus.setTextColor(android.graphics.Color.GREEN)
        btnVpn.text = "DEACTIVATE"
        btnVpn.setBackgroundColor(android.graphics.Color.RED)
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent)
        
        isRunning = false
        txtStatus.text = "SYSTEM READY"
        txtStatus.setTextColor(android.graphics.Color.WHITE)
        btnVpn.text = "ACTIVATE SHIELD"
        btnVpn.setBackgroundColor(android.graphics.Color.BLUE)
    }
}
