package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 101
    private var isRunning = false
    
    private lateinit var btnVpn: Button
    private lateinit var etCustomDns: EditText
    private lateinit var txtStatus: TextView
    private lateinit var txtServerStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVpn = findViewById(R.id.vpnButton)
        etCustomDns = findViewById(R.id.etCustomDns)
        txtStatus = findViewById(R.id.statusText)
        txtServerStatus = findViewById(R.id.serverStatus)

        checkServerConnection()

        btnVpn.setOnClickListener {
            if (!isRunning) {
                val dns = etCustomDns.text.toString().trim()
                if (dns.isEmpty()) {
                    Toast.makeText(this, "Please enter a DNS IP", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpn(dns)
            } else {
                stopVpn()
            }
        }
    }

    private fun checkServerConnection() {
        thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url("https://dns-controller-server.onrender.com/").build()
                val response = client.newCall(request).execute()
                runOnUiThread {
                    if (response.isSuccessful) {
                        txtServerStatus.text = "Server: Online ●"
                        txtServerStatus.setTextColor(android.graphics.Color.GREEN)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    txtServerStatus.text = "Server: Offline ○"
                    txtServerStatus.setTextColor(android.graphics.Color.RED)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val dns = etCustomDns.text.toString().trim()
            startVpn(dns)
        }
    }

    private fun startVpn(dnsIp: String) {
        val intent = Intent(this, MyVpnService::class.java)
        intent.putExtra("DNS_IP", dnsIp)
        startService(intent)
        isRunning = true
        txtStatus.text = "CUSTOM SHIELD ACTIVE"
        txtStatus.setTextColor(android.graphics.Color.GREEN)
        btnVpn.text = "STOP PROTECTION"
        btnVpn.setBackgroundColor(android.graphics.Color.RED)
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent)
        isRunning = false
        txtStatus.text = "PROTECTION INACTIVE"
        txtStatus.setTextColor(android.graphics.Color.RED)
        btnVpn.text = "START CUSTOM SHIELD"
        btnVpn.setBackgroundColor(android.graphics.Color.BLUE)
    }
}
