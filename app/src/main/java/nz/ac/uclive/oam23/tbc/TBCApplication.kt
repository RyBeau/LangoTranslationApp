package nz.ac.uclive.oam23.tbc

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build


class TBCApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(getString(R.string.NOTIFICATION_CHANNEL_ID))
    }

     fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    val database by lazy { TranslationDatabase.getDatabase(this)}
    val repository by lazy { TranslationRepository(database.translationDao()) }
}