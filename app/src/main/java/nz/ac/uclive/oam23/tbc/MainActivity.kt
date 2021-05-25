package nz.ac.uclive.oam23.tbc

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

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
        setTheme(this)
    }

    fun translationSaved() {
        super.onBackPressed()
    }

}