package nz.ac.uclive.oam23.tbc

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager


/**
 * Sets a theme based on SharedPreferences which are changed in the settings page.
 */
fun setTheme(context: Context) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    when (sharedPreferences.getString("themes","OS")) {
        "OS" -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        "Light" -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        "Dark" -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}

/**
 * Turns on/off foreground location tracking and, hence, notifications when you are near a previous translation.
 */
fun setNotifications(context: Context) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    if (sharedPreferences.getBoolean("notifications",false)) {
        startService(context)
    } else {
        stopService(context)
    }
}

private fun startService(context: Context) {
    val intent = Intent(context, NotificationService::class.java)
    context.startService(intent)
}

private fun stopService(context: Context) {
    val intent = Intent(context, NotificationService::class.java)
    context.stopService(intent)
}