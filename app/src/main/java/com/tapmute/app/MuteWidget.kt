package com.tapmute.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class MuteWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE = "com.tapmute.app.TOGGLE_MUTE"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE) {
            val prefs = MutePreferences(context)
            val newState = !prefs.isGlobalMuteEnabled()
            prefs.setGlobalMuteEnabled(newState)

            // Update all widgets
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = android.content.ComponentName(context, MuteWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
            for (id in appWidgetIds) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = MutePreferences(context)
        val isMuted = prefs.isGlobalMuteEnabled()
        val mutedCount = prefs.getMutedApps().size

        val views = RemoteViews(context.packageName, R.layout.widget_mute)

        // Update UI based on state
        if (isMuted) {
            views.setTextViewText(R.id.widgetStatus, "🔇 Sessiz")
            views.setTextViewText(R.id.widgetCount, "$mutedCount uygulama")
            views.setInt(R.id.widgetBackground, "setBackgroundResource", R.drawable.widget_bg_active)
        } else {
            views.setTextViewText(R.id.widgetStatus, "🔔 Açık")
            views.setTextViewText(R.id.widgetCount, "$mutedCount uygulama")
            views.setInt(R.id.widgetBackground, "setBackgroundResource", R.drawable.widget_bg_inactive)
        }

        // Toggle intent
        val toggleIntent = Intent(context, MuteWidget::class.java).apply {
            action = ACTION_TOGGLE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetBackground, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
