package com.osamaalek.kiosklauncher.util

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object DebugLogger {
    private const val TAG = "KioskLauncher"
    private const val LOG_FILE_NAME = "KioskDebug.log"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    
    fun init(context: Context) {
        log("=== KIOSK LAUNCHER DEBUG SESSION STARTED ===")
        log("Device Info:")
        log("- Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        log("- Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        log("- App Version: ${getAppVersion(context)}")
        log("============================================")
    }
    
    fun log(message: String) {
        val timestamp = dateFormat.format(Date())
        val logMessage = "$timestamp: $message"
        
        // Log to Android logcat
        Log.d(TAG, message)
        
        // Write to external file
        writeToFile(logMessage)
    }
    
    fun logError(message: String, throwable: Throwable? = null) {
        val errorMessage = if (throwable != null) {
            "$message: ${throwable.message}\n${throwable.stackTraceToString()}"
        } else {
            message
        }
        
        val timestamp = dateFormat.format(Date())
        val logMessage = "$timestamp ERROR: $errorMessage"
        
        Log.e(TAG, errorMessage)
        writeToFile(logMessage)
    }
    
    fun logStatusBarState(activity: android.app.Activity) {
        log("Status Bar Analysis:")
        log("- Window flags: ${Integer.toBinaryString(activity.window.attributes.flags)}")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            log("- Status bar color: ${Integer.toHexString(activity.window.statusBarColor)}")
            log("- Navigation bar color: ${Integer.toHexString(activity.window.navigationBarColor)}")
        }
        
        val decorView = activity.window.decorView
        log("- System UI visibility: ${Integer.toBinaryString(decorView.systemUiVisibility)}")
        
        // Log status bar height
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            val statusBarHeight = activity.resources.getDimensionPixelSize(resourceId)
            log("- Status bar height: ${statusBarHeight}px")
        } else {
            log("- Status bar height: UNKNOWN")
        }
    }
    
    fun logWebViewState(webView: android.webkit.WebView) {
        log("WebView State:")
        log("- URL: ${webView.url}")
        log("- Layout params: ${webView.layoutParams}")
        log("- Padding: top=${webView.paddingTop}, bottom=${webView.paddingBottom}")
        log("- Size: ${webView.width}x${webView.height}")
        log("- Position: (${webView.x}, ${webView.y})")
    }
    
    private fun writeToFile(message: String) {
        try {
            val externalDir = Environment.getExternalStorageDirectory()
            val logFile = File(externalDir, LOG_FILE_NAME)
            
            FileWriter(logFile, true).use { writer ->
                writer.appendLine(message)
                writer.flush()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file: ${e.message}")
        }
    }
    
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}