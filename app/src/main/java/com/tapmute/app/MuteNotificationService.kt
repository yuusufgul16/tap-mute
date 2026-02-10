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

        // 1. Global Check
        if (!mutePrefs.isGlobalMuteEnabled()) return

        // 2. App-specific Check
        if (!mutePrefs.isMuted(packageName)) return

        // 3. Keyword Check (Smart Filter)
        val extras = sbn.notification.extras
        val keywords = mutePrefs.getKeywords()
        
        if (keywords.isNotEmpty()) {
            // Concatenate all possible text fields for a thorough search
            val contentToSearch = StringBuilder()
            
            contentToSearch.append(extras.getString("android.title") ?: "")
            contentToSearch.append(" ")
            contentToSearch.append(extras.getCharSequence("android.text") ?: "")
            contentToSearch.append(" ")
            contentToSearch.append(extras.getCharSequence("android.bigText") ?: "")
            contentToSearch.append(" ")
            contentToSearch.append(extras.getCharSequence("android.summaryText") ?: "")
            
            // For MessagingStyle (WhatsApp), check the messages array
            val messages = extras.getParcelableArray("android.messages")
            if (messages != null) {
                for (m in messages) {
                    if (m is android.os.Bundle) {
                        contentToSearch.append(" ")
                        contentToSearch.append(m.getCharSequence("text") ?: "")
                    }
                }
            }

            val fullText = contentToSearch.toString().lowercase()
            
            for (keyword in keywords) {
                if (fullText.contains(keyword.lowercase())) {
                    Log.d("TapMute", "Allowed by keyword ($keyword): $packageName")
                    return // Allowed
                }
            }
        }

        // 5. Block & Statistics
        cancelNotification(sbn.key)
        
        // Battery optimization: Only process data storage when necessary
        // When screen is off, we still block but can minimize other work if it was heavier
        mutePrefs.incrementMuteCount(packageName)
        
        Log.d("TapMute", "Blocked notification from: $packageName")
    }

    private fun isScreenOn(): Boolean {
        val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isInteractive
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed
    }
}
