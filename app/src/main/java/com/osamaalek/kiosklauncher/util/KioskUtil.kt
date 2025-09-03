package com.osamaalek.kiosklauncher.util

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import android.widget.Toast
import com.osamaalek.kiosklauncher.MyDeviceAdminReceiver
import com.osamaalek.kiosklauncher.ui.MainActivity
import com.osamaalek.kiosklauncher.service.KioskMonitorService

class KioskUtil {
    companion object {
        fun startKioskMode(context: Activity) {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)

            if (devicePolicyManager.isAdminActive(myDeviceAdmin)) {
                context.startLockTask()
                
                // Mark that we're in kiosk mode and start monitoring service
                val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("was_in_kiosk_mode", true).apply()
                
                // Start the monitoring service for auto-resume functionality
                val autoResumeEnabled = sharedPreferences.getBoolean("auto_resume_kiosk", false)
                if (autoResumeEnabled) {
                    KioskMonitorService.startService(context)
                }
                
                DebugLogger.log("Kiosk mode started - monitoring service status: $autoResumeEnabled")
                
            } else {
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

                //
                val appsWhiteList = arrayOf("com.osamaalek.kiosklauncher")
                devicePolicyManager.setLockTaskPackages(myDeviceAdmin, appsWhiteList)

                devicePolicyManager.addUserRestriction(
                    myDeviceAdmin, UserManager.DISALLOW_UNINSTALL_APPS
                )

            } else {
                Toast.makeText(
                    context, "This app is not an owner device", Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun stopKioskMode(context: Activity) {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)
            if (devicePolicyManager.isAdminActive(myDeviceAdmin)) {
                context.stopLockTask()
                
                // Clear kiosk mode flag and stop monitoring service
                val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("was_in_kiosk_mode", false).apply()
                
                // Stop the monitoring service
                KioskMonitorService.stopService(context)
                
                DebugLogger.log("Kiosk mode stopped - monitoring service stopped")
            }
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                devicePolicyManager.clearUserRestriction(
                    myDeviceAdmin, UserManager.DISALLOW_UNINSTALL_APPS
                )
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
    }
}