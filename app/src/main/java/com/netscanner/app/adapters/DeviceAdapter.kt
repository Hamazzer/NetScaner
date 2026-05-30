package com.netscanner.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netscanner.app.R
import com.netscanner.app.models.Device

class DeviceAdapter(
    private val devices: List<Device>,
    private val onClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvIp: TextView = view.findViewById(R.id.tvIp)
        val tvHostname: TextView = view.findViewById(R.id.tvHostname)
        val tvMac: TextView = view.findViewById(R.id.tvMac)
        val tvVendor: TextView = view.findViewById(R.id.tvVendor)
        val tvLatency: TextView = view.findViewById(R.id.tvLatency)
        val tvDeviceIcon: TextView = view.findViewById(R.id.tvDeviceIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false))

    override fun getItemCount() = devices.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val device = devices[position]
        holder.tvIp.text = device.ip
        holder.tvHostname.text = if (device.hostname != "Unknown") device.hostname else device.ip
        holder.tvMac.text = device.mac
        holder.tvVendor.text = device.vendor
        holder.tvLatency.text = if (device.latency > 0) "${device.latency}ms" else ""
        holder.tvDeviceIcon.text = getDeviceIcon(device)
        holder.itemView.setOnClickListener { onClick(device) }
    }

    private fun getDeviceIcon(device: Device): String {
        val vendor = device.vendor.lowercase()
        val hostname = device.hostname.lowercase()
        return when {
            vendor.contains("apple") || hostname.contains("iphone") || hostname.contains("mac") -> "🍎"
            vendor.contains("samsung") || hostname.contains("samsung") -> "📱"
            vendor.contains("router") || hostname.contains("router") || hostname.contains("gateway") -> "📡"
            vendor.contains("tp-link") || vendor.contains("asus") || vendor.contains("netgear") -> "📡"
            vendor.contains("microsoft") || hostname.contains("windows") -> "🖥"
            vendor.contains("raspberry") -> "🥧"
            else -> "💻"
        }
    }
}
