package com.netscanner.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.netscanner.app.R
import com.netscanner.app.databinding.ActivitySplashBinding
import com.netscanner.app.utils.RootUtils

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.ivLogo.startAnimation(pulse)

        Handler(Looper.getMainLooper()).postDelayed({
            val isRooted = RootUtils.isRooted()
            binding.tvStatus.text = if (isRooted) "Root доступ получен ✓" else "Root не найден — ограниченный режим"
            binding.tvStatus.setTextColor(
                if (isRooted) getColor(R.color.green_accent)
                else getColor(R.color.orange_accent)
            )
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }, 1000)
        }, 1500)
    }
}
