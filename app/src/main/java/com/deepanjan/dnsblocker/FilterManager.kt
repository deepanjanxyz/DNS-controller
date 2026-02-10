package com.deepanjan.dnsblocker

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import kotlin.concurrent.thread

object FilterManager {
    // এই দেখ তোর সার্ভারের রিয়েল লিংক বসিয়ে দিলাম
    private const val SERVER_URL = "https://dns-controller-server.onrender.com/api/blocklist"
    
    // ব্যাকআপ (যদি সার্ভার কখনো স্লো কাজ করে)
    private const val BACKUP_URL = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews-gambling-porn/hosts"
    
    fun downloadFilterList(context: Context, onComplete: (Boolean, Int) -> Unit) {
        thread {
            try {
                Log.d("FilterManager", "Connecting to Render Server...")
                val client = OkHttpClient()
                
                // ১. সার্ভারে হিট করা
                val request = Request.Builder().url(SERVER_URL).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val jsonString = response.body?.string() ?: "{}"
                    val json = JSONObject(jsonString)
                    
                    // সার্ভার থেকে লিস্ট বের করা
                    val domains = json.getJSONArray("domains")
                    val total = domains.length()
                    
                    // ফাইলে সেভ করা
                    val file = File(context.filesDir, "filter_list.txt")
                    file.printWriter().use { out ->
                        for (i in 0 until total) {
                            out.println(domains.getString(i))
                        }
                    }
                    
                    Log.d("FilterManager", "Server Download Success: $total rules")
                    onComplete(true, total)
                    return@thread
                }
            } catch (e: Exception) {
                Log.e("FilterManager", "Server failed, switching to backup: ${e.message}")
            }
            
            // ২. সার্ভার ফেইল করলে গিটহাব থেকে সরাসরি নামাবে
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(BACKUP_URL).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val content = response.body?.string() ?: ""
                    val file = File(context.filesDir, "filter_list.txt")
                    file.writeText(content)
                    
                    val count = content.lines().count { it.startsWith("0.0.0.0") }
                    onComplete(true, count)
                } else {
                    onComplete(false, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, 0)
            }
        }
    }
}
