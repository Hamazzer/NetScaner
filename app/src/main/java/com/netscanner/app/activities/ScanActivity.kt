package com.netscanner.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.netscanner.app.adapters.DeviceAdapter
import com.netscanner.app.databinding.ActivityScanBinding
import com.netscanner.app.models.Device
import com.netscanner.app.models.ScanProgress
import com.netscanner.app.utils.NetworkUtils
import com.netscanner.app.utils.NmapRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private val devices = mutableListOf<Device>()
    private lateinit var adapter: DeviceAdapter
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getStringExtra("mode") ?: "discover"

        adapter = DeviceAdapter(devices) { device ->
            startActivity(Intent(this, DeviceDetailActivity::class.java).apply {
                putExtra("ip", device.ip)
                putExtra("hostname", device.hostname)
                putExtra("mac", device.mac)
                putExtra("vendor", device.vendor)
                putExtra("os", device.os)
            })
        }

        binding.rvDevices.layoutManager = LinearLayoutManager(this)
        binding.rvDevices.adapter = adapter

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = if (mode == "quick") "Быстрое сканирование" else "Сканирование сети"

        binding.btnScan.setOnClickListener {
            if (!isScanning) startScan(mode)
            else stopScan()
        }

        startScan(mode)
    }

    private fun startScan(mode: String) {
        isScanning = true
        devices.clear()
        adapter.notifyDataSetChanged()
        binding.btnScan.text = "Остановить"
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.visibility = View.VISIBLE
        binding.tvFound.text = "Найдено: 0"

        val subnet = NetworkUtils.getSubnet(this)

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                NmapRunner.discoverHosts(subnet) { progress ->
                    runOnUiThread { updateProgress(progress) }
                }
            }
            onScanComplete(result)
        }
    }

    private fun updateProgress(progress: ScanProgress) {
        binding.progressBar.max = progress.total
        binding.progressBar.progress = progress.current
        binding.tvStatus.text = "Проверяю: ${progress.currentHost}"
        binding.tvFound.text = "Найдено: ${progress.found}"
    }

    private fun onScanComplete(result: List<Device>) {
        isScanning = false
        binding.btnScan.text = "Сканировать снова"
        binding.progressBar.visibility = View.GONE
        binding.tvStatus.text = "Сканирование завершено"
        binding.tvFound.text = "Найдено устройств: ${result.size}"
        devices.addAll(result)
        adapter.notifyDataSetChanged()
        binding.tvEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun stopScan() {
        isScanning = false
        binding.btnScan.text = "Сканировать снова"
        binding.progressBar.visibility = View.GONE
        binding.tvStatus.text = "Остановлено"
    }
}
