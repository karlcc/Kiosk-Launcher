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

class KioskUtil {
    companion object {
        
        private const val PREF_KIOSK_AUTO_RESUME = "kiosk_auto_resume"
        private const val PREF_WAS_IN_KIOSK_ON_PAUSE = "was_in_kiosk_on_pause"
        private const val PREF_KIOSK_TEMPORARILY_DISABLED = "kiosk_temporarily_disabled"
        
        fun isAutoResumeEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_KIOSK_AUTO_RESUME, true)
        }
        
        fun setAutoResumeEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_KIOSK_AUTO_RESUME, enabled).apply()
        }
        
        fun setKioskPausedState(context: Context, wasInKiosk: Boolean) {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_WAS_IN_KIOSK_ON_PAUSE, wasInKiosk).apply()
        }
        
        fun wasInKioskModeBeforePause(context: Context): Boolean {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_WAS_IN_KIOSK_ON_PAUSE, false)
        }
        
        fun clearKioskPausedState(context: Context) {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            prefs.edit().remove(PREF_WAS_IN_KIOSK_ON_PAUSE).apply()
        }
        
        fun setKioskTemporarilyDisabled(context: Context, disabled: Boolean) {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_KIOSK_TEMPORARILY_DISABLED, disabled).apply()
        }
        
        fun isKioskTemporarilyDisabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_KIOSK_TEMPORARILY_DISABLED, false)
        }
        
        fun clearTemporaryDisable(context: Context) {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            prefs.edit().remove(PREF_KIOSK_TEMPORARILY_DISABLED).apply()
        }
        fun startKioskMode(context: Activity) {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)

            if (devicePolicyManager.isAdminActive(myDeviceAdmin)) {
                // Set lock task packages to allow smooth exit without unlock prompt
                try {
                    val appsWhiteList = arrayOf("com.osamaalek.kiosklauncher")
                    devicePolicyManager.setLockTaskPackages(myDeviceAdmin, appsWhiteList)
                    Toast.makeText(context, "Smooth kiosk exit enabled", Toast.LENGTH_SHORT).show()
                } catch (e: SecurityException) {
                    // Device Admin insufficient - requires Device Owner for smooth exit
                    Toast.makeText(context, "Note: Device Owner required for smooth exit", Toast.LENGTH_LONG).show()
                }
                
                context.startLockTask()
                
                // Clear temporary disable when successfully starting kiosk mode
                clearTemporaryDisable(context)
                
                // Device owner specific features
                if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                    val filter = IntentFilter(Intent.ACTION_MAIN)
                    filter.addCategory(Intent.CATEGORY_HOME)
                    filter.addCategory(Intent.CATEGORY_DEFAULT)
                    val activity = ComponentName(context, MainActivity::class.java)
                    devicePolicyManager.addPersistentPreferredActivity(myDeviceAdmin, filter, activity)

                    devicePolicyManager.addUserRestriction(
                        myDeviceAdmin, UserManager.DISALLOW_UNINSTALL_APPS
                    )
                }
            } else {
                context.startActivity(
                    Intent().setComponent(
                        ComponentName(
                            "com.android.settings", "com.android.settings.DeviceAdminSettings"
                        )
                    )
                )
            }
        }

        fun stopKioskMode(context: Activity) {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)
            if (devicePolicyManager.isAdminActive(myDeviceAdmin)) {
                context.stopLockTask()
            }
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                devicePolicyManager.clearUserRestriction(
                    myDeviceAdmin, UserManager.DISALLOW_UNINSTALL_APPS
                )
            }
            // Set temporary disable instead of clearing state completely
            setKioskTemporarilyDisabled(context, true)
            clearKioskPausedState(context)
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