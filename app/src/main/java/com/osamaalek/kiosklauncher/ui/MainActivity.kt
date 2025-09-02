package com.osamaalek.kiosklauncher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osamaalek.kiosklauncher.R
import com.osamaalek.kiosklauncher.util.DisplayUtil
import com.osamaalek.kiosklauncher.util.KioskUtil
import com.osamaalek.kiosklauncher.util.ScreenStateReceiver
import com.osamaalek.kiosklauncher.util.DebugLogger

class MainActivity : AppCompatActivity() {
    
    private var screenStateReceiver: ScreenStateReceiver? = null

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
            
            // Enable auto-resume kiosk mode by default (home launcher replacement feature)
            sharedPreferences.edit()
                .putBoolean("first_launch", false)
                .putBoolean("auto_resume_kiosk", true)
                .apply()
            
            DebugLogger.log("First launch - auto-resume kiosk mode enabled by default")
        }
        
        // Handle auto-resume intent from ScreenStateReceiver
        if (intent.getBooleanExtra("auto_resume_kiosk", false)) {
            DebugLogger.log("MainActivity launched for auto-resume - starting kiosk mode")
        }
    }

    override fun onResume() {
        super.onResume()
        DisplayUtil.applyDisplaySettings(this, this)
        
        // Register screen state receiver for auto-resume functionality
        if (screenStateReceiver == null) {
            screenStateReceiver = ScreenStateReceiver.register(this)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Unregister screen state receiver
        ScreenStateReceiver.unregister(this, screenStateReceiver)
        screenStateReceiver = null
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