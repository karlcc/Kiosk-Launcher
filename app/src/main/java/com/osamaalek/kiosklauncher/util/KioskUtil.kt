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
        
        @Volatile
        internal var wasInKioskModeBeforePause = false
        @Volatile
        internal var screenUnlockedSincePause = false
        
        private const val PREF_KIOSK_AUTO_RESUME = "kiosk_auto_resume"
        
        fun isAutoResumeEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_KIOSK_AUTO_RESUME, true)
        }
        
        fun setAutoResumeEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_KIOSK_AUTO_RESUME, enabled).apply()
        }
        
        fun onManualKioskExit() {
            wasInKioskModeBeforePause = false
            screenUnlockedSincePause = false
        }
        fun startKioskMode(context: Activity) {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val myDeviceAdmin = ComponentName(context, MyDeviceAdminReceiver::class.java)

            if (devicePolicyManager.isAdminActive(myDeviceAdmin)) {
                context.startLockTask()
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
            }
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                devicePolicyManager.clearUserRestriction(
                    myDeviceAdmin, UserManager.DISALLOW_UNINSTALL_APPS
                )
            }
            onManualKioskExit()
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