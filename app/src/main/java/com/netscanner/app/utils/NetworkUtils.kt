package com.netscanner.app.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIp(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }

    fun getSubnet(context: Context): String {
        val ip = getLocalIp(context)
        val parts = ip.split(".")
        return if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}" else "192.168.1"
    }

    fun getGateway(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiManager.dhcpInfo
        val gw = dhcp.gateway
        return String.format(
            "%d.%d.%d.%d",
            gw and 0xff,
            gw shr 8 and 0xff,
            gw shr 16 and 0xff,
            gw shr 24 and 0xff
        )
    }

    fun getSsid(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.connectionInfo.ssid.replace("\"", "")
    }

    fun getNetworkRange(context: Context): String {
        return "${getSubnet(context)}.0/24"
    }

    fun isReachable(ip: String, timeout: Int = 1000): Boolean {
        return try {
            InetAddress.getByName(ip).isReachable(timeout)
        } catch (e: Exception) {
            false
        }
    }

    fun reverseDns(ip: String): String {
        return try {
            val addr = InetAddress.getByName(ip)
            val hostname = addr.canonicalHostName
            if (hostname == ip) "Unknown" else hostname
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
