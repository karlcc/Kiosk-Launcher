package com.osamaalek.kiosklauncher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osamaalek.kiosklauncher.R
import com.osamaalek.kiosklauncher.util.DisplayUtil
import com.osamaalek.kiosklauncher.util.KioskUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // DisplayUtil.applyDisplaySettings(this, this) // Disabled - conflicts with fullscreen mode
        KioskUtil.startKioskMode(this)
    }

    override fun onResume() {
        super.onResume()
        // DisplayUtil.applyDisplaySettings(this, this) // Disabled - conflicts with fullscreen mode
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        when (currentFragment) {
            is HomeFragment -> {
                // Check if webview can handle back navigation
                if (!currentFragment.onBackPressed()) {
                    // If webview can't go back, do nothing (kiosk mode should stay)
                    // or could exit app if needed
                }
            }
            is AppsListFragment -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, HomeFragment()).commit()
            is ConfigFragment -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, HomeFragment()).commit()
        }
    }
}