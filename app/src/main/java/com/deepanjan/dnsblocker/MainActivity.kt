package com.deepanjan.dnsblocker

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
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
    private lateinit var btnDownload: Button
    private lateinit var txtStatus: TextView
    private lateinit var txtFilters: TextView
    private lateinit var txtServerStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVpn = findViewById(R.id.vpnButton)
        btnDownload = findViewById(R.id.downloadButton)
        txtStatus = findViewById(R.id.statusText)
        txtFilters = findViewById(R.id.filterStats)
        txtServerStatus = findViewById(R.id.serverStatus)

        // অ্যাপ খুললেই সার্ভার চেক করবে
        checkServerConnection()

        btnDownload.setOnClickListener {
            txtFilters.text = "Fetching from Server..."
            btnDownload.isEnabled = false
            
            FilterManager.downloadFilterList(this) { success, count ->
                runOnUiThread {
                    btnDownload.isEnabled = true
                    if (success) {
                        txtFilters.text = "Active Rules: $count"
                        Toast.makeText(this, "Filters Updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        txtFilters.text = "Update Failed!"
                    }
                }
            }
        }

        btnVpn.setOnClickListener {
            if (!isRunning) {
                val intent = VpnService.prepare(this)
                if (intent != null) startActivityForResult(intent, VPN_REQUEST_CODE)
                else startVpn()
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
                    } else {
                        txtServerStatus.text = "Server: Online (API Issue) ○"
                        txtServerStatus.setTextColor(android.graphics.Color.YELLOW)
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
            startVpn()
        }
    }

    private fun startVpn() {
        startService(Intent(this, MyVpnService::class.java))
        isRunning = true
        txtStatus.text = "PROTECTION ACTIVE"
        txtStatus.setTextColor(android.graphics.Color.GREEN)
        btnVpn.text = "STOP SHIELD"
        btnVpn.setBackgroundColor(android.graphics.Color.RED)
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent)
        isRunning = false
        txtStatus.text = "PROTECTION INACTIVE"
        txtStatus.setTextColor(android.graphics.Color.RED)
        btnVpn.text = "START SHIELD"
        btnVpn.setBackgroundColor(android.graphics.Color.BLUE)
    }
}
