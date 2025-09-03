package com.osamaalek.kiosklauncher.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.osamaalek.kiosklauncher.BuildConfig
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
        
        // Check if we should auto-resume kiosk mode
        // Only auto-resume if not temporarily disabled (user didn't explicitly exit)
        if (KioskUtil.isAutoResumeEnabled(this) && 
            // look like do no required complicate logic, test comment following line
            // KioskUtil.wasInKioskModeBeforePause(this) && 
            !KioskUtil.isKioskTemporarilyDisabled(this)) {
            // Clear the flag immediately to prevent re-triggering
            KioskUtil.clearKioskPausedState(this)
            
            // Post to message queue to ensure onResume completes before changing lock state
            Handler(Looper.getMainLooper()).post {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this, "Auto-resuming kiosk. TempDisabled: ${KioskUtil.isKioskTemporarilyDisabled(this)}", Toast.LENGTH_SHORT).show()
                }
                KioskUtil.startKioskMode(this)
            }
        }
        
        // If user returns to app after temporary disable, clear flag for first time, no direct start kiosk
        if (KioskUtil.isKioskTemporarilyDisabled(this)) {
            Handler(Looper.getMainLooper()).post {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this, "Clearing temp disable. TempDisabled: ${KioskUtil.isKioskTemporarilyDisabled(this)}", Toast.LENGTH_LONG).show()
                }
                KioskUtil.clearTemporaryDisable(this)
            }
        }
        
        DisplayUtil.applyDisplaySettings(this, this)
    }
    
    override fun onPause() {
        super.onPause()
        
        // Only set flag if pausing while in kiosk mode AND not due to configuration changes
        // AND auto-resume is enabled (no point setting flag if auto-resume is disabled)
        if (KioskUtil.isKioskModeActive(this) && 
            !isChangingConfigurations() && 
            KioskUtil.isAutoResumeEnabled(this)) {
            KioskUtil.setKioskPausedState(this, true)
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