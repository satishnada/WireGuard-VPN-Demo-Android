package com.satish.wireguardvpn.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// A singleton object to hold the shared VPN state.
// Both the ViewModel and the Service will access this.
object VpnStateRepository {
    private val _vpnRunning = MutableStateFlow(false)
    val vpnRunning = _vpnRunning.asStateFlow()

    fun setRunning(running: Boolean) {
        _vpnRunning.value = running
    }

}
