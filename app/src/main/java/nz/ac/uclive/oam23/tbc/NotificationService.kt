package nz.ac.uclive.oam23.tbc

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*


class NotificationService : Service() {

    val NOTIFICATION_ID = 1
    val SERVICE_ID = 2

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var location: Location
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    var requestingLocations = false

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

            val notification =
                NotificationCompat.Builder(this, getString(R.string.NOTIFICATION_CHANNEL_ID))
                    .setContentTitle(getString(R.string.service_title)).setContentText(getString(R.string.foreground_notif_msg))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent)
                    .build()

            startForeground(SERVICE_ID, notification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d(
                        "location",
                        location.latitude.toString() + " " + location.longitude.toString()
                    )

                    val lat = location.latitude.toInt()
                    val lon = location.longitude.toInt()

                    val latlngString = "$lat%, $lon%"
                    scope.launch {
                        withContext(Dispatchers.IO) {
                             val nearbyTranslations =
                                TranslationDatabase.getDatabase(applicationContext).translationDao()
                                    .getNearbyTranslations(latlngString)

                            if (nearbyTranslations.isNotEmpty()) {
                                createNotification()

                                for (translation in nearbyTranslations) {
                                    Log.d("nearby", translation.locationString)
                                }
                            } else {
                                removeNotifications()
                            }
                        }
                    }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission Issue", "Permission Issue")
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { recentLocation: Location? ->
                if (recentLocation != null) {
                    location = recentLocation
                }
            }
        }
        setLocationRequest()
        startLocationUpdates(this)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.setInterval(600).priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private fun startLocationUpdates(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            return
        }
        if (!requestingLocations && ::fusedLocationClient.isInitialized && ::locationRequest.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            requestingLocations = true
        }
    }

    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            requestingLocations = false
        }
        removeNotifications()
    }

    private fun createNotification() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifications = mNotificationManager.activeNotifications

        for (notification in notifications) {
            if (notification.id == NOTIFICATION_ID) {
                return
            }
        }

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, getString(R.string.NOTIFICATION_CHANNEL_ID))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_content))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun removeNotifications() {
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFICATION_ID)
        }
    }

}