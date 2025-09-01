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

class ConfigFragment : Fragment() {

    private lateinit var fabApps: FloatingActionButton
    private lateinit var imageButtonExit: ImageButton
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSaveUrl: Button
    private lateinit var buttonBackToHome: Button
    private lateinit var buttonChangePassword: Button
    private lateinit var textViewCurrentPassword: TextView
    private lateinit var switchFullscreen: Switch
    private lateinit var switchHideStatusBar: Switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_config, container, false)

        fabApps = v.findViewById(R.id.floatingActionButton_config)
        imageButtonExit = v.findViewById(R.id.imageButton_exit_config)
        editTextUrl = v.findViewById(R.id.editText_url)
        buttonSaveUrl = v.findViewById(R.id.button_save_url)
        buttonBackToHome = v.findViewById(R.id.button_back_to_home)
        buttonChangePassword = v.findViewById(R.id.button_change_password)
        textViewCurrentPassword = v.findViewById(R.id.textView_current_password)
        switchFullscreen = v.findViewById(R.id.switch_fullscreen)
        switchHideStatusBar = v.findViewById(R.id.switch_hide_status_bar)

        sharedPreferences = requireContext().getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)

        // Load saved settings
        val savedUrl = sharedPreferences.getString("webview_url", "https://www.google.com")
        editTextUrl.setText(savedUrl)

        val (hideStatusBar, fullscreenMode) = DisplayUtil.getDisplaySettings(requireContext())
        switchFullscreen.isChecked = fullscreenMode
        switchHideStatusBar.isChecked = hideStatusBar

        val currentPassword = PasswordDialog.getCurrentPassword(requireContext())
        textViewCurrentPassword.text = "Current password: $currentPassword"

        fabApps.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, AppsListFragment()).commit()
        }

        imageButtonExit.setOnClickListener {
            KioskUtil.stopKioskMode(requireActivity())
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

        buttonChangePassword.setOnClickListener {
            PasswordDialog.showSetPasswordDialog(requireContext()) {
                val newPassword = PasswordDialog.getCurrentPassword(requireContext())
                textViewCurrentPassword.text = "Current password: $newPassword"
            }
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

        buttonBackToHome.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, HomeFragment()).commit()
        }

        return v
    }
}