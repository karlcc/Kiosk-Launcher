package com.osamaalek.kiosklauncher.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.osamaalek.kiosklauncher.R
import com.osamaalek.kiosklauncher.util.DisplayUtil
import com.osamaalek.kiosklauncher.util.KioskUtil
import com.osamaalek.kiosklauncher.util.DeviceIdentifier

class ConfigFragment : Fragment() {

    private lateinit var fabApps: FloatingActionButton
    private lateinit var fabStartKiosk: FloatingActionButton
    private lateinit var fabExitKiosk: FloatingActionButton
    private lateinit var fabChangePassword: FloatingActionButton
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSaveUrl: Button
    private lateinit var buttonBackToHome: Button
    private lateinit var textViewCurrentPassword: TextView
    private lateinit var switchFullscreen: Switch
    private lateinit var switchHideStatusBar: Switch
    private lateinit var switchAutoResume: Switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_config, container, false)

        fabApps = v.findViewById(R.id.floatingActionButton_config)
        fabStartKiosk = v.findViewById(R.id.fab_start_kiosk)
        fabExitKiosk = v.findViewById(R.id.fab_exit_kiosk)
        fabChangePassword = v.findViewById(R.id.fab_change_password)
        editTextUrl = v.findViewById(R.id.editText_url)
        buttonSaveUrl = v.findViewById(R.id.button_save_url)
        buttonBackToHome = v.findViewById(R.id.button_back_to_home)
        textViewCurrentPassword = v.findViewById(R.id.textView_current_password)
        switchFullscreen = v.findViewById(R.id.switch_fullscreen)
        switchHideStatusBar = v.findViewById(R.id.switch_hide_status_bar)
        switchAutoResume = v.findViewById(R.id.switch_auto_resume)

        sharedPreferences = requireContext().getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)

        // Load saved settings
        val savedUrl = sharedPreferences.getString("webview_url", "https://www.google.com")
        editTextUrl.setText(savedUrl)

        val (hideStatusBar, fullscreenMode) = DisplayUtil.getDisplaySettings(requireContext())
        switchFullscreen.isChecked = fullscreenMode
        switchHideStatusBar.isChecked = hideStatusBar
        
        val autoResumeEnabled = KioskUtil.isAutoResumeEnabled(requireContext())
        switchAutoResume.isChecked = autoResumeEnabled

        val currentPassword = PasswordDialog.getCurrentPassword(requireContext())
        val deviceInfo = DeviceIdentifier.getDeviceIdentifierWithInfo(requireContext())
        textViewCurrentPassword.text = "Current password: $currentPassword\n\nDevice ID: ${deviceInfo.deviceId}\nDevice Info: ${deviceInfo.deviceInfo}"

        fabApps.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, AppsListFragment()).commit()
        }

        fabStartKiosk.setOnClickListener {
            KioskUtil.startKioskMode(requireActivity())
        }

        fabExitKiosk.setOnClickListener {
            // Clear auto-resume state BEFORE stopping kiosk to prevent loop
            KioskUtil.clearKioskPausedState(requireContext())
            // stopKioskMode will set temporary disable automatically
            KioskUtil.stopKioskMode(requireActivity())
        }

        fabChangePassword.setOnClickListener {
            PasswordDialog.showSetPasswordDialog(requireContext()) {
                val newPassword = PasswordDialog.getCurrentPassword(requireContext())
                textViewCurrentPassword.text = "Current password: $newPassword"
            }
        }

        buttonSaveUrl.setOnClickListener {
            val url = editTextUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                // Add http:// if no protocol is specified
                val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else {
                    url
                }
                
                sharedPreferences.edit().putString("webview_url", formattedUrl).apply()
                Toast.makeText(context, "URL saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        }

        // Long press to toggle device validation (for testing)
        buttonSaveUrl.setOnLongClickListener {
            val currentStatus = sharedPreferences.getBoolean("device_validation_enabled", false)
            val newStatus = !currentStatus
            sharedPreferences.edit().putBoolean("device_validation_enabled", newStatus).apply()
            
            val statusText = if (newStatus) "ENABLED" else "DISABLED"
            Toast.makeText(context, "Device Validation: $statusText", Toast.LENGTH_LONG).show()
            true
        }


        switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchHideStatusBar.isChecked = false
            }
            DisplayUtil.saveDisplaySettings(requireContext(), switchHideStatusBar.isChecked, isChecked)
            DisplayUtil.applyDisplaySettings(requireActivity(), requireContext())
        }

        switchHideStatusBar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchFullscreen.isChecked = false
            }
            DisplayUtil.saveDisplaySettings(requireContext(), isChecked, switchFullscreen.isChecked)
            DisplayUtil.applyDisplaySettings(requireActivity(), requireContext())
        }

        switchAutoResume.setOnCheckedChangeListener { _, isChecked ->
            KioskUtil.setAutoResumeEnabled(requireContext(), isChecked)
        }

        buttonBackToHome.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, HomeFragment()).commit()
        }

        return v
    }
}