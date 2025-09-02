package com.osamaalek.kiosklauncher.util

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import android.view.WindowManager
import android.widget.Toast
import com.osamaalek.kiosklauncher.MyDeviceAdminReceiver
import com.osamaalek.kiosklauncher.ui.MainActivity

class KioskUtil {
    companion object {
        fun startKioskMode(context: Activity) {
            DebugLogger.log("KioskUtil: Starting kiosk mode")
            
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)

            // Enable fullscreen mode when starting kiosk (if user preference allows)
            enableKioskFullscreen(context)

            if (devicePolicyManager.isAdminActive(myDeviceAdmin)) {
                context.startLockTask()
                DebugLogger.log("KioskUtil: Lock task started")
            } else {
                DebugLogger.log("KioskUtil: Device admin not active, opening settings")
                context.startActivity(
                    Intent().setComponent(
                        ComponentName(
                            "com.android.settings", "com.android.settings.DeviceAdminSettings"
                        )
                    )
                )
            }
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                val filter = IntentFilter(Intent.ACTION_MAIN)
                filter.addCategory(Intent.CATEGORY_HOME)
                filter.addCategory(Intent.CATEGORY_DEFAULT)
                val activity = ComponentName(context, MainActivity::class.java)
                devicePolicyManager.addPersistentPreferredActivity(myDeviceAdmin, filter, activity)

                val appsWhiteList = arrayOf("com.osamaalek.kiosklauncher")
                devicePolicyManager.setLockTaskPackages(myDeviceAdmin, appsWhiteList)

                devicePolicyManager.addUserRestriction(
                    myDeviceAdmin, UserManager.DISALLOW_UNINSTALL_APPS
                )
                
                DebugLogger.log("KioskUtil: Device owner restrictions applied")
            } else {
                DebugLogger.log("KioskUtil: App is not device owner")
                Toast.makeText(
                    context, "This app is not an owner device", Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun stopKioskMode(context: Activity) {
            DebugLogger.log("KioskUtil: Stopping kiosk mode")
            
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)
            
            // Restore normal status bar when exiting kiosk
            disableKioskFullscreen(context)
            
            if (devicePolicyManager.isAdminActive(myDeviceAdmin)) {
                context.stopLockTask()
                DebugLogger.log("KioskUtil: Lock task stopped")
            }
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                devicePolicyManager.clearUserRestriction(
                    myDeviceAdmin, UserManager.DISALLOW_UNINSTALL_APPS
                )
                DebugLogger.log("KioskUtil: User restrictions cleared")
            }
        }

        fun isKioskModeActive(context: Activity): Boolean {
            return try {
                val devicePolicyManager =
                    context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)
                devicePolicyManager.isAdminActive(myDeviceAdmin)
            } catch (e: Exception) {
                false
            }
        }

        fun toggleKioskMode(context: Activity) {
            if (isKioskModeActive(context)) {
                stopKioskMode(context)
            } else {
                startKioskMode(context)
            }
        }
        
        private fun enableKioskFullscreen(activity: Activity) {
            try {
                // Check user preference for fullscreen mode
                val sharedPreferences = activity.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
                val fullscreenEnabled = sharedPreferences.getBoolean("kiosk_fullscreen_enabled", true)
                
                DebugLogger.log("KioskUtil: Enabling kiosk mode with fullscreen: $fullscreenEnabled")
                
                if (fullscreenEnabled) {
                    // Apply fullscreen approach (hide status bar)
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    
                    // Clear conflicting flags
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    
                    DebugLogger.log("KioskUtil: Fullscreen kiosk flags applied")
                } else {
                    // Normal mode - keep status bar visible
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    
                    DebugLogger.log("KioskUtil: Normal kiosk mode (status bar visible)")
                }
                
                DebugLogger.logStatusBarState(activity)
                
            } catch (e: Exception) {
                DebugLogger.logError("KioskUtil: Error configuring kiosk display mode", e)
            }
        }
        
        private fun disableKioskFullscreen(activity: Activity) {
            try {
                DebugLogger.log("KioskUtil: Disabling kiosk fullscreen mode")
                
                // Clear fullscreen flags to restore normal status bar
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                
                // Force status bar to be visible
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                
                DebugLogger.log("KioskUtil: Normal status bar restored")
                DebugLogger.logStatusBarState(activity)
                
            } catch (e: Exception) {
                DebugLogger.logError("KioskUtil: Error disabling kiosk fullscreen", e)
            }
        }
    }
}