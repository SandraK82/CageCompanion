package com.cagecompanion.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cagecompanion.CageCompanionApp
import com.cagecompanion.R
import com.cagecompanion.data.CageColor
import com.cagecompanion.data.CageStatus
import com.cagecompanion.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CageWidgetReceiver : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            val app = context.applicationContext as CageCompanionApp
            val prefs = app.preferencesManager

            val cageTime = prefs.lastCageTime.first()
            val yellowThreshold = prefs.yellowThresholdMinutes.first()
            val redThreshold = prefs.redThresholdMinutes.first()

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, cageTime, yellowThreshold, redThreshold)
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            cageTimeMs: Long,
            yellowThresholdMinutes: Int,
            redThresholdMinutes: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_cage)

            // Set click intent to open main activity
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            if (cageTimeMs > 0) {
                val cageStatus = CageStatus.fromTimestamp(
                    cageTimeMs,
                    yellowThresholdMinutes,
                    redThresholdMinutes
                )

                views.setTextViewText(R.id.widget_cage_value, cageStatus.formattedAge)

                val colorRes = when (cageStatus.color) {
                    CageColor.GREEN -> R.color.cage_green
                    CageColor.YELLOW -> R.color.cage_yellow
                    CageColor.RED -> R.color.cage_red
                }
                views.setInt(R.id.widget_container, "setBackgroundColor", context.getColor(colorRes))

                val textColor = when (cageStatus.color) {
                    CageColor.YELLOW -> context.getColor(R.color.black)
                    else -> context.getColor(R.color.white)
                }
                views.setTextColor(R.id.widget_cage_value, textColor)
                views.setTextColor(R.id.widget_cage_label, textColor)
            } else {
                views.setTextViewText(R.id.widget_cage_value, context.getString(R.string.cage_no_data))
                views.setInt(R.id.widget_container, "setBackgroundColor", context.getColor(R.color.gray))
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CageWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
