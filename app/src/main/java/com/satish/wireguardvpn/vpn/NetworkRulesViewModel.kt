package com.satish.wireguardvpn.vpn

import android.app.Application
import androidx.lifecycle.AndroidViewModel

// The ViewModel now gets its data from the singleton repository.
class NetworkRulesViewModel(app: Application) : AndroidViewModel(app) {
    // Expose the flows from the repository directly
    val vpnRunning = VpnStateRepository.vpnRunning

}
