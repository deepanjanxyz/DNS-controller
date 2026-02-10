package com.nextvpn.adsblocker
import android.os.Bundle
import android.content.Intent
import android.net.VpnService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Button(onClick = {
                val intent = VpnService.prepare(this@MainActivity)
                if (intent != null) startActivityForResult(intent, 0)
                else startService(Intent(this@MainActivity, AdBlockVpnService::class.java))
            }) { Text("Start Net-Shield") }
        }
    }
}
