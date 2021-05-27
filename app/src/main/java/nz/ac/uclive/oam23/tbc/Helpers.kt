package nz.ac.uclive.oam23.tbc

import android.content.Context
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