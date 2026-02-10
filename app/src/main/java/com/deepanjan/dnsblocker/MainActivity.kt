package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isRunning = false
    
    private lateinit var btnVpn: Button
    private lateinit var btnWhitelist: Button
    private lateinit var switchAds: Switch
    private lateinit var switchFamily: Switch
    private lateinit var switchCustom: Switch
    private lateinit var etCustomDns: EditText
    private lateinit var txtStatus: TextView
    
    // Stats Views
    private lateinit var txtTotalReq: TextView
    private lateinit var txtBlocked: TextView
    private lateinit var txtDataSaved: TextView

    // ব্রডকাস্ট রিসিভার (ডাটা আপডেট করার জন্য)
    private val statsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val total = intent?.getLongExtra("TOTAL", 0) ?: 0
            val blocked = intent?.getLongExtra("BLOCKED", 0) ?: 0
            val savedMb = String.format("%.1f MB", blocked * 0.5)

            txtTotalReq.text = total.toString()
            txtBlocked.text = blocked.toString()
            txtDataSaved.text = savedMb
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI কানেকশন
        btnVpn = findViewById(R.id.vpnButton)
        btnWhitelist = findViewById(R.id.btnWhitelist)
        switchAds = findViewById(R.id.switchAds)
        switchFamily = findViewById(R.id.switchFamily)
        switchCustom = findViewById(R.id.switchCustom)
        etCustomDns = findViewById(R.id.etCustomDns)
        txtStatus = findViewById(R.id.statusText)
        txtTotalReq = findViewById(R.id.txtTotalReq)
        txtBlocked = findViewById(R.id.txtBlocked)
        txtDataSaved = findViewById(R.id.txtDataSaved)

        LocalBroadcastManager.getInstance(this).registerReceiver(statsReceiver, IntentFilter("VPN_STATS_UPDATE"))

        // লজিক আগের মতোই...
        switchCustom.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchAds.isChecked = false
                switchFamily.isChecked = false
                etCustomDns.visibility = View.VISIBLE
            } else {
                etCustomDns.visibility = View.GONE
            }
        }

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
        
        btnWhitelist.setOnClickListener {
            Toast.makeText(this, "Whitelist feature coming soon!", Toast.LENGTH_SHORT).show()
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
                dnsToUse = "94.140.14.15"
                modeName = "Ultra Shield"
            } else if (ads) {
                dnsToUse = "94.140.14.14"
                modeName = "Ad-Blocker"
            } else if (family) {
                dnsToUse = "1.1.1.3"
                modeName = "Family Safe"
            } else {
                Toast.makeText(this, "Select a filter!", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statsReceiver)
    }
}
