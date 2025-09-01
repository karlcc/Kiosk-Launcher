package com.osamaalek.kiosklauncher.ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class PasswordDialog {
    companion object {
        private const val DEFAULT_PASSWORD = "admin123"
        private const val PASSWORD_KEY = "settings_password"

        fun showPasswordDialog(context: Context, onSuccess: () -> Unit, onCancel: (() -> Unit)? = null) {
            val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            val storedPassword = sharedPreferences.getString(PASSWORD_KEY, DEFAULT_PASSWORD)

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Enter Settings Password")
            builder.setMessage("Please enter the password to access settings")

            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)

            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.hint = "Password"
            layout.addView(input)

            builder.setView(layout)

            builder.setPositiveButton("OK") { _, _ ->
                val enteredPassword = input.text.toString()
                if (enteredPassword == storedPassword) {
                    onSuccess()
                } else {
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                onCancel?.invoke()
            }

            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.show()
        }

        fun showSetPasswordDialog(context: Context, onSuccess: (() -> Unit)? = null) {
            val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Set New Password")
            builder.setMessage("Enter a new password for settings access")

            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)

            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.hint = "New Password"
            layout.addView(input)

            builder.setView(layout)

            builder.setPositiveButton("Save") { _, _ ->
                val newPassword = input.text.toString().trim()
                if (newPassword.isNotEmpty() && newPassword.length >= 4) {
                    sharedPreferences.edit().putString(PASSWORD_KEY, newPassword).apply()
                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    onSuccess?.invoke()
                } else {
                    Toast.makeText(context, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.create().show()
        }

        fun getCurrentPassword(context: Context): String {
            val sharedPreferences = context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)
            return sharedPreferences.getString(PASSWORD_KEY, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
        }
    }
}