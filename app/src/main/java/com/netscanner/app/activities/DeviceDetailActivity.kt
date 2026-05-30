package com.netscanner.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.netscanner.app.adapters.PortAdapter
import com.netscanner.app.databinding.ActivityDeviceDetailBinding
import com.netscanner.app.models.Port
import com.netscanner.app.utils.NmapRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceDetailBinding
    private val ports = mutableListOf<Port>()
    private lateinit var portAdapter: PortAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ip = intent.getStringExtra("ip") ?: return
        val hostname = intent.getStringExtra("hostname") ?: "Unknown"
        val mac = intent.getStringExtra("mac") ?: "Unknown"
        val vendor = intent.getStringExtra("vendor") ?: "Unknown"
        val os = intent.getStringExtra("os") ?: "Unknown"

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = ip

        binding.tvHostname.text = hostname
        binding.tvIp.text = ip
        binding.tvMac.text = mac
        binding.tvVendor.text = vendor
        binding.tvOs.text = os

        portAdapter = PortAdapter(ports)
        binding.rvPorts.layoutManager = LinearLayoutManager(this)
        binding.rvPorts.adapter = portAdapter

        binding.btnPortScan.setOnClickListener {
            startActivity(Intent(this, PortScanActivity::class.java).apply {
                putExtra("ip", ip)
            })
        }

        binding.btnQuickScan.setOnClickListener {
            runQuickScan(ip)
        }

        binding.btnFullScan.setOnClickListener {
            runFullScan(ip)
        }
    }

    private fun runQuickScan(ip: String) {
        binding.progressScan.visibility = View.VISIBLE
        binding.tvScanStatus.text = "Быстрое сканирование портов..."
        binding.tvScanStatus.visibility = View.VISIBLE
        ports.clear()
        portAdapter.notifyDataSetChanged()

        lifecycleScope.launch {
            val device = withContext(Dispatchers.IO) { NmapRunner.quickScan(ip) }
            binding.progressScan.visibility = View.GONE
            binding.tvScanStatus.text = "Найдено портов: ${device.openPorts.size}"
            if (device.os != "Unknown") binding.tvOs.text = device.os
            ports.addAll(device.openPorts)
            portAdapter.notifyDataSetChanged()
            binding.tvNoPorts.visibility = if (ports.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun runFullScan(ip: String) {
        binding.progressScan.visibility = View.VISIBLE
        binding.tvScanStatus.text = "Полное сканирование (может занять время)..."
        binding.tvScanStatus.visibility = View.VISIBLE
        ports.clear()
        portAdapter.notifyDataSetChanged()

        lifecycleScope.launch {
            val device = withContext(Dispatchers.IO) { NmapRunner.fullScan(ip) }
            binding.progressScan.visibility = View.GONE
            binding.tvScanStatus.text = "Найдено портов: ${device.openPorts.size}"
            if (device.os != "Unknown") binding.tvOs.text = device.os
            ports.addAll(device.openPorts)
            portAdapter.notifyDataSetChanged()
            binding.tvNoPorts.visibility = if (ports.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
