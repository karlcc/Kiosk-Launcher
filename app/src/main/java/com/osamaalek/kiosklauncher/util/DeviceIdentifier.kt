package com.osamaalek.kiosklauncher.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.osamaalek.kiosklauncher.BuildConfig

object DeviceIdentifier {
    
    /**
     * Get the Android ID for the device
     * This is persistent across app reinstalls but changes on factory reset
     */
    fun getDeviceId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        } catch (e: Exception) {
            DebugLogger.logError("Failed to get Android ID", e)
            "unknown"
        }
    }
    
    /**
     * Get device information for server logging/debugging
     */
    fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER}_${Build.MODEL}_Android${Build.VERSION.RELEASE}"
    }
    
    /**
     * Get app version for server compatibility checks
     */
    fun getAppVersion(): String {
        return "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
    }
    
    /**
     * Get comprehensive device identifier with info
     */
    fun getDeviceIdentifierWithInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            deviceId = getDeviceId(context),
            deviceInfo = getDeviceInfo(),
            appVersion = getAppVersion()
        )
    }
}

data class DeviceInfo(
    val deviceId: String,
    val deviceInfo: String,
    val appVersion: String
)