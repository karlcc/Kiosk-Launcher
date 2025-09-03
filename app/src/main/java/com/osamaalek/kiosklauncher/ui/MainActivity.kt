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

        // Initialize default display settings on first launch
        initializeDefaultSettings()
        
        DisplayUtil.applyDisplaySettings(this, this)
        KioskUtil.startKioskMode(this)
    }
    
    private fun initializeDefaultSettings() {
        val sharedPreferences = getSharedPreferences("kiosk_settings", MODE_PRIVATE)
        
        // Check if this is first launch by looking for a "first_launch" key
        val isFirstLaunch = sharedPreferences.getBoolean("first_launch", true)
        
        if (isFirstLaunch) {
            // Set optimal default settings for kiosk mode
            DisplayUtil.saveDisplaySettings(this, false, true) // hideStatusBar=false, fullscreenMode=true
            
            // Mark that we've initialized the settings
            sharedPreferences.edit().putBoolean("first_launch", false).apply()
        }
    }

    override fun onResume() {
        super.onResume()
        DisplayUtil.applyDisplaySettings(this, this)
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