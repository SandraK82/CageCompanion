package com.cagecompanion

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.cagecompanion.data.PreferencesManager
import com.cagecompanion.network.NightscoutApi

class CageCompanionApp : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var nightscoutApi: NightscoutApi
        private set

    override fun onCreate() {
        super.onCreate()

        preferencesManager = PreferencesManager(this)
        nightscoutApi = NightscoutApi(preferencesManager)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "cage_status"
        const val NOTIFICATION_ID = 1001
    }
}
