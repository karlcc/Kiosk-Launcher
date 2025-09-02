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
import com.osamaalek.kiosklauncher.util.KioskUtil
import com.osamaalek.kiosklauncher.util.DebugLogger

class ConfigFragment : Fragment() {

    private lateinit var fabApps: FloatingActionButton
    private lateinit var fabStartKiosk: FloatingActionButton
    private lateinit var fabExitKiosk: FloatingActionButton
    private lateinit var fabChangePassword: FloatingActionButton
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSaveUrl: Button
    private lateinit var buttonBackToHome: Button
    private lateinit var textViewCurrentPassword: TextView
    private lateinit var switchFullscreenKiosk: Switch
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
        switchFullscreenKiosk = v.findViewById(R.id.switch_fullscreen_kiosk)

        sharedPreferences = requireContext().getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)

        // Load saved settings
        val savedUrl = sharedPreferences.getString("webview_url", "https://www.google.com")
        editTextUrl.setText(savedUrl)

        // Load fullscreen preference
        val fullscreenEnabled = sharedPreferences.getBoolean("kiosk_fullscreen_enabled", true)
        switchFullscreenKiosk.isChecked = fullscreenEnabled

        val currentPassword = PasswordDialog.getCurrentPassword(requireContext())
        textViewCurrentPassword.text = "Current password: $currentPassword"
        
        DebugLogger.log("ConfigFragment: Fullscreen kiosk setting loaded: $fullscreenEnabled")

        fabApps.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, AppsListFragment()).commit()
        }

        fabStartKiosk.setOnClickListener {
            DebugLogger.log("ConfigFragment: Starting kiosk mode")
            KioskUtil.startKioskMode(requireActivity())
        }

        fabExitKiosk.setOnClickListener {
            DebugLogger.log("ConfigFragment: Exiting kiosk mode")
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

        switchFullscreenKiosk.setOnCheckedChangeListener { _, isChecked ->
            DebugLogger.log("ConfigFragment: Fullscreen kiosk setting changed to: $isChecked")
            sharedPreferences.edit().putBoolean("kiosk_fullscreen_enabled", isChecked).apply()
            Toast.makeText(context, if (isChecked) "Fullscreen mode enabled for kiosk" else "Normal mode enabled for kiosk", Toast.LENGTH_SHORT).show()
        }

        buttonBackToHome.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, HomeFragment()).commit()
        }

        return v
    }
}