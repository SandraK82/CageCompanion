package com.cagecompanion.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cagecompanion.CageCompanionApp
import com.cagecompanion.R
import com.cagecompanion.data.CageColor
import com.cagecompanion.data.CageStatus
import com.cagecompanion.ui.MainActivity

object CageNotificationService {

    fun updateNotification(context: Context, cageStatus: CageStatus) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val colorRes = when (cageStatus.color) {
            CageColor.GREEN -> R.color.cage_green
            CageColor.YELLOW -> R.color.cage_yellow
            CageColor.RED -> R.color.cage_red
        }

        val notification = NotificationCompat.Builder(context, CageCompanionApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(cageStatus.formattedAge)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setColor(context.getColor(colorRes))
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                CageCompanionApp.NOTIFICATION_ID,
                notification
            )
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(CageCompanionApp.NOTIFICATION_ID)
    }
}
