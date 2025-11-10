package com.satish.wireguardvpn

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.satish.wireguardvpn.ui.theme.WireguardVPNTheme
import com.satish.wireguardvpn.vpn.MyWireGuardService
import com.satish.wireguardvpn.vpn.NetworkRulesViewModel

class MainActivity : ComponentActivity() {

    private val vpnPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If result is OK or null intent (already prepared), we can start service
        startService(Intent(this, MyWireGuardService::class.java).apply {
            action = MyWireGuardService.ACTION_START_TUNNEL
        })
    }

    // 1. Create a launcher for the notification permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted.")
                startVpn()
            } else {
                Log.d("MainActivity", "Notification permission denied.")
                // Even if denied, we can still start the VPN.
                // The system will show a generic notification on Android 14+.
                startVpn()
            }
        }

    private fun startVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermission.launch(intent)
        } else {
            // Permission already granted, start the service directly
            startService(Intent(this, MyWireGuardService::class.java).apply {
                action = MyWireGuardService.ACTION_START_TUNNEL
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WireguardVPNTheme {

                val vm = remember { NetworkRulesViewModel(application) }
                HomeScreen(
                    isRunning = vm.vpnRunning.collectAsState().value,
                    onToggle = {
                        if (!vm.vpnRunning.value) {
                            // Check for notification permission before starting VPN
                            if (ContextCompat.checkSelfPermission(
                                    this, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                startVpn()
                            } else {
                                // Request permission. The result callback will handle starting the VPN.
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            stopService(Intent(this, MyWireGuardService::class.java))
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHomeScreen() {
    WireguardVPNTheme {
        HomeScreen(isRunning = false, onToggle = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHomeScreenRunning() {
    WireguardVPNTheme {
        HomeScreen(
            isRunning = true,
            onToggle = {}
        )
    }
}


@Composable
private fun HomeScreen(
    isRunning: Boolean,
    onToggle: () -> Unit
) {
    var newDomain by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("WireGuard VPN (Skeleton)", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onToggle) {
                Text(if (isRunning) "Disconnect" else "Connect")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}