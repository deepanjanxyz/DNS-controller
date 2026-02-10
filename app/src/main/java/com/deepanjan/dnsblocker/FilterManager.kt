package com.deepanjan.dnsblocker

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.concurrent.thread

object FilterManager {
    // হ্যাকার এবং ম্যালওয়্যার ব্লকলিস্ট (বিখ্যাত StevenBlack লিস্ট)
    private const val HACKER_LIST_URL = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews-gambling-porn/hosts"
    
    fun downloadFilterList(context: Context, onComplete: (Boolean, Int) -> Unit) {
        thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(HACKER_LIST_URL).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val content = response.body?.string() ?: ""
                    val file = File(context.filesDir, "filter_list.txt")
                    file.writeText(content)
                    
                    // কতগুলো হ্যাকার সাইট পাওয়া গেল তা গোনা হচ্ছে
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
