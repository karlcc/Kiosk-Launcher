package com.osamaalek.kiosklauncher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osamaalek.kiosklauncher.ui.MainActivity
import com.osamaalek.kiosklauncher.util.KioskUtil

class KioskActivationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle the intent action
        when (intent.action) {
            ACTION_START_KIOSK -> {
                KioskUtil.startKioskMode(this)
                // Launch main activity to show the kiosk interface
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            ACTION_STOP_KIOSK -> {
                KioskUtil.stopKioskMode(this)
                finish()
            }
            else -> {
                // Default behavior - launch main activity
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }

    companion object {
        const val ACTION_START_KIOSK = "com.osamaalek.kiosklauncher.START_KIOSK"
        const val ACTION_STOP_KIOSK = "com.osamaalek.kiosklauncher.STOP_KIOSK"
    }
}