package nz.ac.uclive.oam23.tbc

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE)

class MainActivity: AppCompatActivity() {

    private lateinit var location : Location

    /**
     * Enum for locations representing each fragment.
     */
    enum class Location{
        HOME, PREFERENCES, PREVIOUS_TRANSLATIONS, SAVE_EDIT_TRANSLATION, VIEW_TRANSLATION
    }

    /**
     * Handler for back pressed, uses location to decide on how to handle
     * event.
     */
    override fun onBackPressed() {
        when(location){
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
    private fun onBackConfirmation(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.go_back_confirmation))
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton(R.string.no){ dialog, _ ->
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
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.sum()) {
                Toast.makeText(this, getString(R.string.permissions_granted), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_LONG).show()
            }
            val string = grantResults.map {it.toString() }.toTypedArray()
            Log.d("Test:", string.contentToString())
        }
    }

    fun checkPermissions(){
        if(!hasPermissions(this)){
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }

    companion object {
        fun hasPermissions(context: Context): Boolean = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

}