package com.osamaalek.kiosklauncher.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager

class DisplayUtil {
    companion object {
        private const val HIDE_STATUS_BAR_KEY = "hide_status_bar"
        private const val FULLSCREEN_MODE_KEY = "fullscreen_mode"

        fun setFullscreenMode(activity: Activity, enabled: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowInsetsController = activity.window.insetsController
                if (enabled) {
                    // Enable layout behind system bars for seamless fullscreen
                    activity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    )
                    windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    windowInsetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                }
            } else {
                @Suppress("DEPRECATION")
                if (enabled) {
                    activity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    )
                    activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
                }
            }
        }

        fun setStatusBarVisibility(activity: Activity, visible: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowInsetsController = activity.window.insetsController
                if (visible) {
                    windowInsetsController?.show(WindowInsets.Type.statusBars())
                } else {
                    windowInsetsController?.hide(WindowInsets.Type.statusBars())
                }
            } else {
                @Suppress("DEPRECATION")
                if (visible) {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                } else {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            }
        }

        fun applyDisplaySettings(activity: Activity, context: Context) {
            val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            val hideStatusBar = sharedPreferences.getBoolean(HIDE_STATUS_BAR_KEY, true)
            val fullscreenMode = sharedPreferences.getBoolean(FULLSCREEN_MODE_KEY, true)

            if (fullscreenMode) {
                setFullscreenMode(activity, true)
            } else {
                setStatusBarVisibility(activity, !hideStatusBar)
            }
        }

        fun saveDisplaySettings(context: Context, hideStatusBar: Boolean, fullscreenMode: Boolean) {
            val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean(HIDE_STATUS_BAR_KEY, hideStatusBar)
                .putBoolean(FULLSCREEN_MODE_KEY, fullscreenMode)
                .apply()
        }

        fun getDisplaySettings(context: Context): Pair<Boolean, Boolean> {
            val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            val hideStatusBar = sharedPreferences.getBoolean(HIDE_STATUS_BAR_KEY, true)
            val fullscreenMode = sharedPreferences.getBoolean(FULLSCREEN_MODE_KEY, true)
            return Pair(hideStatusBar, fullscreenMode)
        }
    }
}