package com.cagecompanion.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cagecompanion.CageCompanionApp
import com.cagecompanion.R
import com.cagecompanion.data.CageColor
import com.cagecompanion.data.CageStatus
import com.cagecompanion.ui.MainActivity
import com.cagecompanion.widget.CageWidgetReceiver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class CageUpdateService : Service() {

    companion object {
        private const val TAG = "CageUpdateService"
        private const val UPDATE_INTERVAL_MS = 60_000L // 1 minute
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var updateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Start as foreground service with initial notification
        startForeground(CageCompanionApp.NOTIFICATION_ID, createNotification(null))

        // Start periodic updates
        startPeriodicUpdates()

        return START_STICKY
    }

    private fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                updateCageDisplay()
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    private suspend fun updateCageDisplay() {
        try {
            val app = application as CageCompanionApp
            val prefs = app.preferencesManager

            val cageTime = prefs.lastCageTime.first()
            val yellowThreshold = prefs.yellowThresholdMinutes.first()
            val redThreshold = prefs.redThresholdMinutes.first()

            if (cageTime > 0) {
                val cageStatus = CageStatus.fromTimestamp(cageTime, yellowThreshold, redThreshold)

                // Update notification
                val notification = createNotification(cageStatus)
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.notify(CageCompanionApp.NOTIFICATION_ID, notification)

                // Update widgets
                updateWidgets(cageStatus, yellowThreshold, redThreshold)

                Log.d(TAG, "Updated CAGE display: ${cageStatus.formattedAge}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating CAGE display", e)
        }
    }

    private fun createNotification(cageStatus: CageStatus?): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = getString(R.string.app_name)
        val text = cageStatus?.formattedAge ?: getString(R.string.cage_no_data)

        val colorRes = when (cageStatus?.color) {
            CageColor.GREEN -> R.color.cage_green
            CageColor.YELLOW -> R.color.cage_yellow
            CageColor.RED -> R.color.cage_red
            null -> R.color.gray
        }

        return NotificationCompat.Builder(this, CageCompanionApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("CAGE: $text")
            .setStyle(NotificationCompat.BigTextStyle().bigText("CAGE: $text"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setColor(getColor(colorRes))
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setShowWhen(false)
            .build()
    }

    private fun updateWidgets(cageStatus: CageStatus, yellowThreshold: Int, redThreshold: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetComponent = ComponentName(this, CageWidgetReceiver::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        for (widgetId in widgetIds) {
            CageWidgetReceiver.updateAppWidget(
                this,
                appWidgetManager,
                widgetId,
                cageStatus.cageTimeMs,
                yellowThreshold,
                redThreshold
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}
