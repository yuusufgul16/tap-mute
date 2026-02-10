package com.tapmute.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MuteNotificationService : NotificationListenerService() {

    private lateinit var mutePrefs: MutePreferences

    // Phone/dialer packages that should NEVER be muted
    private val phonePackages = setOf(
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.samsung.android.incallui",
        "com.android.incallui",
        "com.android.phone",
        "com.android.server.telecom"
    )

    override fun onCreate() {
        super.onCreate()
        mutePrefs = MutePreferences(this)
        Log.d("TapMute", "NotificationListenerService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName

        // Never block phone/dialer notifications
        if (packageName in phonePackages) return

        // Check if global mute is enabled and if this app is in the muted list
        if (mutePrefs.isGlobalMuteEnabled() && mutePrefs.isMuted(packageName)) {
            cancelNotification(sbn.key)
            Log.d("TapMute", "Blocked notification from: $packageName")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed
    }
}
