package com.osamaalek.kiosklauncher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.osamaalek.kiosklauncher.R
import com.osamaalek.kiosklauncher.ui.MainActivity
import com.osamaalek.kiosklauncher.util.DebugLogger

class KioskMonitorService : Service() {

    private var screenUnlockReceiver: BroadcastReceiver? = null
    private val notificationId = 1001
    private val channelId = "kiosk_monitor_channel"

    override fun onCreate() {
        super.onCreate()
        DebugLogger.log("KioskMonitorService created")
        createNotificationChannel()
        registerScreenUnlockReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugLogger.log("KioskMonitorService started")
        
        val notification = createNotification()
        startForeground(notificationId, notification)
        
        return START_STICKY // Restart service if killed
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterScreenUnlockReceiver()
        DebugLogger.log("KioskMonitorService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Kiosk Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors screen unlock for kiosk mode auto-resume"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            DebugLogger.log("Notification channel created for KioskMonitorService")
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Kiosk Mode Active")
            .setContentText("Monitoring for auto-resume after screen unlock")
            .setSmallIcon(R.drawable.baseline_screen_lock_portrait_24)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun registerScreenUnlockReceiver() {
        screenUnlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_USER_PRESENT -> {
                        DebugLogger.log("Screen unlocked - checking auto-resume settings")
                        checkAndResumeKioskMode()
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        }

        registerReceiver(screenUnlockReceiver, filter)
        DebugLogger.log("Screen unlock receiver registered")
    }

    private fun unregisterScreenUnlockReceiver() {
        screenUnlockReceiver?.let {
            try {
                unregisterReceiver(it)
                screenUnlockReceiver = null
                DebugLogger.log("Screen unlock receiver unregistered")
            } catch (e: Exception) {
                DebugLogger.logError("Failed to unregister screen unlock receiver", e)
            }
        }
    }

    private fun checkAndResumeKioskMode() {
        val sharedPreferences = getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
        val autoResumeEnabled = sharedPreferences.getBoolean("auto_resume_kiosk", false)
        val wasInKioskMode = sharedPreferences.getBoolean("was_in_kiosk_mode", false)

        DebugLogger.log("Auto-resume enabled: $autoResumeEnabled, Was in kiosk: $wasInKioskMode")

        if (autoResumeEnabled && wasInKioskMode) {
            DebugLogger.log("Conditions met - launching MainActivity to resume kiosk mode")
            
            val launchIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("auto_resume_kiosk", true)
            }

            try {
                startActivity(launchIntent)
                DebugLogger.log("Successfully launched MainActivity for auto-resume")
            } catch (e: Exception) {
                DebugLogger.logError("Failed to launch MainActivity for auto-resume", e)
            }
        } else {
            DebugLogger.log("Auto-resume conditions not met - skipping")
        }
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, KioskMonitorService::class.java)
            context.startForegroundService(intent)
            DebugLogger.log("KioskMonitorService start requested")
        }

        fun stopService(context: Context) {
            val intent = Intent(context, KioskMonitorService::class.java)
            context.stopService(intent)
            DebugLogger.log("KioskMonitorService stop requested")
        }
    }
}