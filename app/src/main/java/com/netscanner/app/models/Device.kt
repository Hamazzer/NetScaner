package com.netscanner.app.models

data class Device(
    val ip: String,
    val mac: String = "Unknown",
    val hostname: String = "Unknown",
    val vendor: String = "Unknown",
    val os: String = "Unknown",
    val openPorts: List<Port> = emptyList(),
    val isOnline: Boolean = true,
    val latency: Long = 0
)

data class Port(
    val number: Int,
    val protocol: String = "tcp",
    val state: String = "open",
    val service: String = "unknown",
    val version: String = ""
)

data class ScanResult(
    val devices: List<Device>,
    val scanTime: Long,
    val network: String
)

data class ScanProgress(
    val current: Int,
    val total: Int,
    val currentHost: String = "",
    val found: Int = 0
)
