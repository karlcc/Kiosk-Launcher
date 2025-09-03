package com.osamaalek.kiosklauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.osamaalek.kiosklauncher.util.KioskUtil

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            KioskUtil.screenUnlockedSincePause = true
        }
    }
}