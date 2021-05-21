package nz.ac.uclive.oam23.tbc

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var location: Location

    /**
     * Enum for locations representing each fragment.
     */
    enum class Location {
        HOME, PREFERENCES, PREVIOUS_TRANSLATIONS, SAVE_EDIT_TRANSLATION, VIEW_TRANSLATION, PROCESSING
    }

    /**
     * Handler for back pressed, uses location to decide on how to handle
     * event.
     */
    override fun onBackPressed() {
        when (location) {
            Location.SAVE_EDIT_TRANSLATION -> {
                onBackConfirmation()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    /**
     * Setter for location property
     */
    fun setLocation(currentLocation: Location) {
        location = currentLocation
    }

    /**
     * Getter for location property
     */
    fun getLocation(): Location {
        return location
    }

    /**
     * Sets a theme based on SharedPreferences which are changed in the settings page.
     */
     fun setTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
        when (sharedPreferences.getString("themes","OS")) {
            "OS" -> {
                setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            }
            "Light" -> {
                setDefaultNightMode(MODE_NIGHT_NO)
            }
            "Dark" -> {
                setDefaultNightMode(MODE_NIGHT_YES)
            }
        }

    }

    /**
     * Creates confirmation dialog for exiting the NewRoundFragment
     */
    private fun onBackConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.go_back_confirmation))
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
        val alert = builder.create()
        alert.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        // Binds navigation controller to the navbar.
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_preferences, R.id.navigation_previous))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        setTheme()
    }

}