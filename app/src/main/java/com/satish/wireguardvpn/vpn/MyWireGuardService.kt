package com.satish.wireguardvpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.satish.wireguardvpn.MainActivity
import com.satish.wireguardvpn.R
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Peer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MyWireGuardService : VpnService() {

    // Using a private companion object is a common pattern for constants internal to a class.
    private companion object {
        private const val NOTIF_CHANNEL = "vpn"
        private const val NOTIF_ID = 42
        private const val NOTIF_CHANNEL_NAME = "VPN Status"
        private const val TAG = "MyWireGuardService"
        const val ACTION_START_TUNNEL = "com.satish.vpn.action.START_TUNNEL"
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var tunInterface: ParcelFileDescriptor? = null
    private var backend: GoBackend? = null
    private val tunnel: Tunnel = WireGuardTunnel()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TUNNEL -> {
                // Update the running state in the repository
                VpnStateRepository.setRunning(true)
                startTunnel()
            }
            else -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        scope.launch {
            try {
                // Update the running state in the repository
                VpnStateRepository.setRunning(false)
                backend?.setState(tunnel, Tunnel.State.DOWN, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error shutting down backend", e)
            }
        }
        tunInterface?.close()
        tunInterface = null
        backend = null
        scope.cancel()
        try {
            tunInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tunInterface = null
        scope.cancel()
        super.onDestroy()
    }

    private fun startTunnel() {
        ensureNotification()

        // 1. Create a WireGuard Configuration
        val config = createWireGuardConfig()
        // 2. Initialize the GoBackend
        // This must be done *before* Builder.establish()
        backend = GoBackend(this)

        // 3. Establish the VpnService TUN interface
        val builder = Builder()
            .setSession("WireGuard-Tunnel")
            .setMtu(1280) // Standard MTU for WireGuard
            .addAddress("10.0.0.20",32)
            .addDnsServer("1.1.1.1")

        // Route all traffic through the tunnel
        builder.addRoute("0.0.0.0", 0)

        scope.launch(Dispatchers.IO) {
            // Now, build the TUN interface and start the backend *inside* the coroutine
            // after the route has been excluded.
            tunInterface = builder.establish()
            Log.i(TAG, "TUN established = ${tunInterface != null}")
            if (tunInterface == null) {
                Log.e(TAG, "Failed to establish TUN interface.")
                stopSelf() // This will trigger onDestroy for cleanup
                return@launch
            }

            // 4. Start the WireGuard backend with the TUN file descriptor
            backend?.setState(tunnel, Tunnel.State.UP, config)
        }
    }


    private fun createWireGuardConfig(): Config {
        // This is where you would load your dynamic or saved configuration.
        // For this example, we'll create a hardcoded one.
        // WARNING: Do NOT hardcode private keys in a real application!
        return Config.Builder()
            .setInterface(
                com.wireguard.config.Interface.Builder()
                    .parsePrivateKey("sdfrtyjke345s2asdfsdfCNjETzEOB3wOUCsFU=") // Replace with a real private key
                    .addAddress(InetNetwork.parse("10.0.0.20/32"))
                    .addDnsServer(java.net.InetAddress.getByName("1.1.1.1"))
                    .build()
            )
            .addPeer(
                Peer.Builder()
                    .parsePublicKey("asfghPAtV9fghfgh42hE=") // Replace with the server's public key
                    .setEndpoint(InetEndpoint.parse("2.91.244.92:51820")) // Replace with your server IP/host and port
                    .addAllowedIp(InetNetwork.parse("0.0.0.0/0")) // Route all traffic through the peer
                    .build()
            )
            .build()
    }

    /**
     * Ensures the notification channel is created and displays the foreground service notification.
     * A foreground service is required for VpnService.
     */
    private fun ensureNotification() {
        // Use context.getSystemService with the class for type safety and to avoid casting.
        val notificationManager = getSystemService(NotificationManager::class.java)
            ?: return // Defensive check in case the system service is not available.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL,
                NOTIF_CHANNEL_NAME, // Provide a user-visible name for the channel.
                NotificationManager.IMPORTANCE_LOW // Low importance is suitable for ongoing status.
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notif: Notification = NotificationCompat.Builder(this, NOTIF_CHANNEL)
            .setContentTitle("WireGuard VPN")
            .setContentText("Connected")
            .setSmallIcon(R.drawable.outline_admin_panel_settings_24)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(NOTIF_ID, notif)
    }

    // --- Inner class to implement the Tunnel interface ---
    private inner class WireGuardTunnel : Tunnel {
        override fun getName() = "MyWireGuardTunnel"

        override fun onStateChange(newState: Tunnel.State) {
            Log.d(TAG, "Tunnel state changed to: $newState")
            if (newState == Tunnel.State.DOWN) {
                stopSelf()
            }
        }

    }
}