@file:Suppress("DEPRECATION")

package com.example.qr.wifiConnection

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import org.koin.core.KoinComponent
import org.koin.core.inject

class WifiConnectionManager : KoinComponent {
    private val context: Context by inject()

    fun connect(SSID: String, password: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager

            val wifiConfig = WifiConfiguration()

            wifiConfig.SSID = String.format("\"%s\"", SSID)
            wifiConfig.preSharedKey = String.format("\"%s\"", password)

            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

            if (!wifiManager.isWifiEnabled)
                wifiManager.isWifiEnabled = true

            val netId = wifiManager.addNetwork(wifiConfig)
            wifiManager.enableNetwork(netId, true)
        }
    }

}