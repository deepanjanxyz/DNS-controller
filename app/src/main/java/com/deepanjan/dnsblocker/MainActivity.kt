package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.*
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
    private lateinit var switchAds: Switch
    private lateinit var switchFamily: Switch
    private lateinit var switchCustom: Switch
    private lateinit var etCustomDns: EditText
    private lateinit var txtStatus: TextView
    private lateinit var txtTotalReq: TextView
    private lateinit var txtBlocked: TextView
    private lateinit var txtDataSaved: TextView

    private val statsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            txtTotalReq.text = intent?.getLongExtra("TOTAL", 0).toString()
            txtBlocked.text = intent?.getLongExtra("BLOCKED", 0).toString()
            val blocked = intent?.getLongExtra("BLOCKED", 0) ?: 0
            txtDataSaved.text = String.format("%.1f MB", blocked * 0.5)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // আইডিগুলো একদম নিখুঁতভাবে ধরা হচ্ছে
        btnVpn = findViewById(R.id.vpnButton)
        switchAds = findViewById(R.id.switchAds)
        switchFamily = findViewById(R.id.switchFamily)
        switchCustom = findViewById(R.id.switchCustom)
        etCustomDns = findViewById(R.id.etCustomDns)
        txtStatus = findViewById(R.id.statusText)
        txtTotalReq = findViewById(R.id.txtTotalReq)
        txtBlocked = findViewById(R.id.txtBlocked)
        txtDataSaved = findViewById(R.id.txtDataSaved)

        LocalBroadcastManager.getInstance(this).registerReceiver(statsReceiver, IntentFilter("VPN_STATS_UPDATE"))

        switchCustom.setOnCheckedChangeListener { _, isChecked ->
            etCustomDns.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) { switchAds.isChecked = false; switchFamily.isChecked = false }
        }

        btnVpn.setOnClickListener {
            if (!isRunning) {
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpnProcess()
            } else {
                stopVpnProcess()
            }
        }
    }

    private fun startVpnProcess() {
        val dns = if (switchCustom.isChecked) etCustomDns.text.toString() else "94.140.14.14"
        val intent = Intent(this, MyVpnService::class.java).putExtra("DNS_IP", dns)
        startService(intent)
        updateUI(true)
    }

    private fun stopVpnProcess() {
        startService(Intent(this, MyVpnService::class.java).setAction("STOP"))
        updateUI(false)
    }

    private fun updateUI(active: Boolean) {
        isRunning = active
        txtStatus.text = if (active) "SHIELD ACTIVE" else "SYSTEM READY"
        txtStatus.setTextColor(if (active) android.graphics.Color.GREEN else android.graphics.Color.WHITE)
        btnVpn.text = if (active) "STOP" else "START"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) startVpnProcess()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statsReceiver)
        super.onDestroy()
    }
}
