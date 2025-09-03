package com.osamaalek.kiosklauncher.ui

import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osamaalek.kiosklauncher.R
import com.osamaalek.kiosklauncher.receiver.ScreenUnlockReceiver
import com.osamaalek.kiosklauncher.util.DisplayUtil
import com.osamaalek.kiosklauncher.util.KioskUtil

class MainActivity : AppCompatActivity() {

    private val screenUnlockReceiver = ScreenUnlockReceiver()

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
        
        // Register the receiver to listen for unlock events
        val filter = IntentFilter(android.content.Intent.ACTION_USER_PRESENT)
        registerReceiver(screenUnlockReceiver, filter)
        
        // Check if we should re-enter kiosk mode
        if (KioskUtil.isAutoResumeEnabled(this) &&
            KioskUtil.wasInKioskModeBeforePause &&
            KioskUtil.screenUnlockedSincePause) {
            
            KioskUtil.startKioskMode(this)
        }
        
        // Always reset flags on resume to prevent stale state
        KioskUtil.wasInKioskModeBeforePause = false
        KioskUtil.screenUnlockedSincePause = false
        
        DisplayUtil.applyDisplaySettings(this, this)
    }
    
    override fun onPause() {
        super.onPause()
        
        // Set the flag if we are pausing while in kiosk mode
        if (KioskUtil.isKioskModeActive(this)) {
            KioskUtil.wasInKioskModeBeforePause = true
        }
        
        // Unregister the receiver to save resources and prevent leaks
        try {
            unregisterReceiver(screenUnlockReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered, ignore
        }
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