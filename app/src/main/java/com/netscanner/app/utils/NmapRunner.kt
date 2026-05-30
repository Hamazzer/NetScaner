package com.netscanner.app.utils

import com.netscanner.app.models.Device
import com.netscanner.app.models.Port
import com.netscanner.app.models.ScanProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object NmapRunner {

    private val COMMON_PORTS = listOf(
        21, 22, 23, 25, 53, 80, 110, 119, 123, 143,
        161, 194, 443, 445, 465, 514, 515, 587, 993,
        995, 1080, 1194, 1433, 1723, 3306, 3389, 5432,
        5900, 6881, 8080, 8443, 8888, 9090, 27017
    )

    private val SERVICE_MAP = mapOf(
        21 to "FTP", 22 to "SSH", 23 to "Telnet",
        25 to "SMTP", 53 to "DNS", 80 to "HTTP",
        110 to "POP3", 143 to "IMAP", 443 to "HTTPS",
        445 to "SMB", 3306 to "MySQL", 3389 to "RDP",
        5432 to "PostgreSQL", 5900 to "VNC", 8080 to "HTTP-Alt",
        8443 to "HTTPS-Alt", 27017 to "MongoDB", 1433 to "MSSQL",
        6881 to "BitTorrent", 1080 to "SOCKS5", 1194 to "OpenVPN",
        123 to "NTP", 161 to "SNMP", 515 to "LPD",
        587 to "SMTP-TLS", 993 to "IMAPS", 995 to "POP3S"
    )

    fun isNmapAvailable(): Boolean {
        val result = RootUtils.runAsRoot("which nmap")
        return result.contains("/nmap")
    }

    fun discoverHosts(subnet: String, onProgress: (ScanProgress) -> Unit): List<Device> {
        val devices = mutableListOf<Device>()
        val total = 254

        if (isNmapAvailable()) {
            devices.addAll(nmapDiscover(subnet, onProgress))
        } else {
            devices.addAll(pingDiscover(subnet, total, onProgress))
        }
        return devices
    }

    private fun nmapDiscover(subnet: String, onProgress: (ScanProgress) -> Unit): List<Device> {
        onProgress(ScanProgress(0, 1, "Запуск nmap...", 0))
        val output = RootUtils.runAsRoot("nmap -sn --open -T4 $subnet.0/24 2>&1")
        return parseNmapHostDiscovery(output)
    }

    private fun pingDiscover(subnet: String, total: Int, onProgress: (ScanProgress) -> Unit): List<Device> {
        val devices = mutableListOf<Device>()
        for (i in 1..254) {
            val ip = "$subnet.$i"
            onProgress(ScanProgress(i, total, ip, devices.size))
            val start = System.currentTimeMillis()
            if (NetworkUtils.isReachable(ip, 300)) {
                val latency = System.currentTimeMillis() - start
                val hostname = NetworkUtils.reverseDns(ip)
                val mac = getMac(ip)
                devices.add(Device(ip = ip, hostname = hostname, mac = mac, latency = latency))
            }
        }
        return devices
    }

    fun scanPorts(ip: String, onProgress: (Int, Int) -> Unit): List<Port> {
        return if (isNmapAvailable()) {
            nmapPortScan(ip, onProgress)
        } else {
            manualPortScan(ip, onProgress)
        }
    }

    fun fullScan(ip: String): Device {
        val output = RootUtils.runAsRoot("nmap -sV -O --open -T4 -p- $ip 2>&1")
        return parseNmapFullScan(ip, output)
    }

    fun quickScan(ip: String): Device {
        val output = RootUtils.runAsRoot("nmap -sV --open -T4 -p ${COMMON_PORTS.joinToString(",")} $ip 2>&1")
        return parseNmapFullScan(ip, output)
    }

    private fun nmapPortScan(ip: String, onProgress: (Int, Int) -> Unit): List<Port> {
        onProgress(0, 1)
        val output = RootUtils.runAsRoot("nmap -sV --open -T4 -p- $ip 2>&1")
        onProgress(1, 1)
        return parseNmapPorts(output)
    }

    private fun manualPortScan(ip: String, onProgress: (Int, Int) -> Unit): List<Port> {
        val openPorts = mutableListOf<Port>()
        COMMON_PORTS.forEachIndexed { index, port ->
            onProgress(index, COMMON_PORTS.size)
            try {
                val socket = java.net.Socket()
                socket.connect(java.net.InetSocketAddress(ip, port), 500)
                socket.close()
                openPorts.add(
                    Port(
                        number = port,
                        service = SERVICE_MAP[port] ?: "unknown",
                        state = "open"
                    )
                )
            } catch (e: Exception) {
                // closed
            }
        }
        return openPorts
    }

    private fun getMac(ip: String): String {
        return try {
            val arp = RootUtils.runAsRoot("cat /proc/net/arp | grep $ip")
            val parts = arp.trim().split(Regex("\\s+"))
            if (parts.size >= 4) parts[3] else "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun parseNmapHostDiscovery(output: String): List<Device> {
        val devices = mutableListOf<Device>()
        val lines = output.lines()
        var currentIp = ""
        var currentMac = "Unknown"
        var currentVendor = "Unknown"
        var currentHostname = "Unknown"

        for (line in lines) {
            when {
                line.startsWith("Nmap scan report for") -> {
                    if (currentIp.isNotEmpty()) {
                        devices.add(Device(
                            ip = currentIp,
                            mac = currentMac,
                            vendor = currentVendor,
                            hostname = currentHostname
                        ))
                    }
                    val parts = line.removePrefix("Nmap scan report for ").trim()
                    val ipMatch = Regex("(\\d+\\.\\d+\\.\\d+\\.\\d+)").find(parts)
                    currentIp = ipMatch?.value ?: parts
                    currentHostname = if (parts.contains("(")) parts.substringBefore("(").trim() else "Unknown"
                    currentMac = "Unknown"
                    currentVendor = "Unknown"
                }
                line.contains("MAC Address:") -> {
                    val macMatch = Regex("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})").find(line)
                    currentMac = macMatch?.value ?: "Unknown"
                    val vendorMatch = Regex("\\((.+)\\)").find(line)
                    currentVendor = vendorMatch?.groupValues?.get(1) ?: "Unknown"
                }
            }
        }
        if (currentIp.isNotEmpty()) {
            devices.add(Device(
                ip = currentIp,
                mac = currentMac,
                vendor = currentVendor,
                hostname = currentHostname
            ))
        }
        return devices
    }

    private fun parseNmapPorts(output: String): List<Port> {
        val ports = mutableListOf<Port>()
        for (line in output.lines()) {
            val match = Regex("^(\\d+)/(tcp|udp)\\s+(open)\\s+(\\S+)(.*)").find(line.trim())
            if (match != null) {
                val (num, proto, state, service, version) = match.destructured
                ports.add(Port(
                    number = num.toInt(),
                    protocol = proto,
                    state = state,
                    service = service,
                    version = version.trim()
                ))
            }
        }
        return ports
    }

    private fun parseNmapFullScan(ip: String, output: String): Device {
        val ports = parseNmapPorts(output)
        var os = "Unknown"
        var hostname = "Unknown"
        var mac = "Unknown"
        var vendor = "Unknown"

        for (line in output.lines()) {
            when {
                line.contains("OS details:") -> os = line.substringAfter("OS details:").trim()
                line.contains("Running:") && os == "Unknown" -> os = line.substringAfter("Running:").trim()
                line.startsWith("Nmap scan report for") -> {
                    val parts = line.removePrefix("Nmap scan report for ").trim()
                    if (parts.contains("(")) hostname = parts.substringBefore("(").trim()
                }
                line.contains("MAC Address:") -> {
                    val macMatch = Regex("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})").find(line)
                    mac = macMatch?.value ?: "Unknown"
                    val vendorMatch = Regex("\\((.+)\\)").find(line)
                    vendor = vendorMatch?.groupValues?.get(1) ?: "Unknown"
                }
            }
        }

        return Device(
            ip = ip,
            mac = mac,
            hostname = hostname,
            vendor = vendor,
            os = os,
            openPorts = ports
        )
    }
}
