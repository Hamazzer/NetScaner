package com.netscanner.app.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.netscanner.app.R
import com.netscanner.app.databinding.ActivityMainBinding
import com.netscanner.app.utils.NetworkUtils
import com.netscanner.app.utils.RootUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERM_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        updateNetworkInfo()

        binding.btnScanNetwork.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java).apply {
                putExtra("mode", "discover")
            })
        }

        binding.btnPortScan.setOnClickListener {
            startActivity(Intent(this, PortScanActivity::class.java))
        }

        binding.btnQuickScan.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java).apply {
                putExtra("mode", "quick")
            })
        }

        binding.tvRootStatus.apply {
            val rooted = RootUtils.isRooted()
            text = if (rooted) "ROOT: ACTIVE" else "ROOT: NOT FOUND"
            setTextColor(
                if (rooted) ContextCompat.getColor(context, R.color.green_accent)
                else ContextCompat.getColor(context, R.color.orange_accent)
            )
        }
    }

    private fun updateNetworkInfo() {
        try {
            binding.tvNetworkName.text = NetworkUtils.getSsid(this)
            binding.tvLocalIp.text = NetworkUtils.getLocalIp(this)
            binding.tvGateway.text = NetworkUtils.getGateway(this)
            binding.tvSubnet.text = NetworkUtils.getNetworkRange(this)
        } catch (e: Exception) {
            binding.tvNetworkName.text = "Нет соединения"
        }
    }

    private fun checkPermissions() {
        val perms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERM_REQUEST)
        }
    }

    override fun onResume() {
        super.onResume()
        updateNetworkInfo()
    }
}
