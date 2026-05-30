package com.netscanner.app.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.netscanner.app.adapters.PortAdapter
import com.netscanner.app.databinding.ActivityPortScanBinding
import com.netscanner.app.models.Port
import com.netscanner.app.utils.NmapRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortScanBinding
    private val ports = mutableListOf<Port>()
    private lateinit var adapter: PortAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefilledIp = intent.getStringExtra("ip") ?: ""

        adapter = PortAdapter(ports)
        binding.rvPorts.layoutManager = LinearLayoutManager(this)
        binding.rvPorts.adapter = adapter

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.etTarget.setText(prefilledIp)

        binding.btnScan.setOnClickListener {
            val ip = binding.etTarget.text.toString().trim()
            if (ip.isNotEmpty()) startScan(ip)
        }

        if (prefilledIp.isNotEmpty()) startScan(prefilledIp)
    }

    private fun startScan(ip: String) {
        ports.clear()
        adapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Сканирование $ip..."
        binding.tvStatus.visibility = View.VISIBLE
        binding.btnScan.isEnabled = false

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                NmapRunner.scanPorts(ip) { current, total ->
                    runOnUiThread {
                        binding.progressBar.max = total
                        binding.progressBar.progress = current
                        binding.tvStatus.text = "Порт $current / $total"
                    }
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.tvStatus.text = "Открытых портов: ${result.size}"
            binding.btnScan.isEnabled = true
            ports.addAll(result)
            adapter.notifyDataSetChanged()
            binding.tvEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
