package com.osamaalek.kiosklauncher.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.osamaalek.kiosklauncher.ui.MainActivity

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                DebugLogger.log("Screen unlocked - checking if kiosk mode should resume")
                
                val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
                val shouldAutoResume = sharedPreferences.getBoolean("auto_resume_kiosk", false)
                val wasInKioskMode = sharedPreferences.getBoolean("was_in_kiosk_mode", false)
                
                DebugLogger.log("Auto resume enabled: $shouldAutoResume, Was in kiosk: $wasInKioskMode")
                
                if (shouldAutoResume && wasInKioskMode) {
                    // Launch MainActivity to resume kiosk mode
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("auto_resume_kiosk", true)
                    }
                    
                    try {
                        context.startActivity(launchIntent)
                        DebugLogger.log("Launched MainActivity for kiosk mode auto-resume")
                    } catch (e: Exception) {
                        DebugLogger.logError("Failed to auto-resume kiosk mode", e)
                    }
                }
            }
        }
    }

    companion object {
        fun register(context: Context): ScreenStateReceiver {
            val receiver = ScreenStateReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            context.registerReceiver(receiver, filter)
            DebugLogger.log("ScreenStateReceiver registered")
            return receiver
        }

        fun unregister(context: Context, receiver: ScreenStateReceiver?) {
            receiver?.let {
                try {
                    context.unregisterReceiver(it)
                    DebugLogger.log("ScreenStateReceiver unregistered")
                } catch (e: Exception) {
                    DebugLogger.logError("Failed to unregister ScreenStateReceiver", e)
                }
            }
        }
    }
}