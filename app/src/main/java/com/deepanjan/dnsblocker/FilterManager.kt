package com.deepanjan.dnsblocker

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import kotlin.concurrent.thread

object FilterManager {
    // এখানে তোর Render সার্ভারের লিংক দিবি
    // আপাতত আমি একটা ডামি লিংক দিচ্ছি, তুই Render থেকে লিংক পেলে এটা পাল্টে দিবি
    private const val SERVER_URL = "https://your-app-name.onrender.com/api/blocklist"
    
    // যদি সার্ভার না চলে, তবে ব্যাকআপ হিসেবে আগের লিংক কাজ করবে
    private const val BACKUP_URL = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews-gambling-porn/hosts"
    
    fun downloadFilterList(context: Context, onComplete: (Boolean, Int) -> Unit) {
        thread {
            try {
                val client = OkHttpClient()
                
                // ১. প্রথমে আমাদের Render সার্ভারে ট্রাই করা
                val request = Request.Builder().url(SERVER_URL).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val jsonString = response.body?.string() ?: "{}"
                    val json = JSONObject(jsonString)
                    
                    // সার্ভার থেকে ডোমেইন লিস্ট বের করা
                    val domains = json.getJSONArray("domains")
                    val total = domains.length()
                    
                    // ফাইলে সেভ করা (লাইন বাই লাইন)
                    val file = File(context.filesDir, "filter_list.txt")
                    file.printWriter().use { out ->
                        for (i in 0 until total) {
                            out.println(domains.getString(i))
                        }
                    }
                    
                    onComplete(true, total)
                    return@thread
                }
            } catch (e: Exception) {
                Log.e("FilterManager", "Server failed, trying backup: ${e.message}")
            }
            
            // ২. সার্ভার ফেইল করলে ব্যাকআপ (সরাসরি GitHub থেকে)
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
