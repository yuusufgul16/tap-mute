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

        // 3. Keyword Check (Strict Latest Content Filter)
        val extras = sbn.notification.extras
        val keywords = mutePrefs.getKeywords()
        
        if (keywords.isNotEmpty()) {
            val latestContent = StringBuilder()
            
            // 1. Current Text (Usually contains the latest message)
            latestContent.append(extras.getCharSequence("android.text") ?: "")
            
            // 2. MessagingStyle (Check ONLY the LAST message in the bundle)
            try {
                val messages = extras.getParcelableArray("android.messages")
                if (messages != null && messages.isNotEmpty()) {
                    val lastMsgBundle = messages.last()
                    if (lastMsgBundle is android.os.Bundle) {
                        latestContent.append(" ")
                        latestContent.append(lastMsgBundle.getCharSequence("text") ?: "")
                    }
                }
            } catch (e: Exception) {
                Log.e("TapMute", "Error reading last message", e)
            }

            // 3. Text Lines (For grouped notifications, check ONLY the LAST line)
            val textLines = extras.getCharSequenceArray("android.textLines")
            if (textLines != null && textLines.isNotEmpty()) {
                latestContent.append(" ")
                latestContent.append(textLines.last())
            }

            val searchStr = latestContent.toString().lowercase()
            
            var matched = false
            for (keyword in keywords) {
                if (searchStr.contains(keyword.lowercase())) {
                    matched = true
                    break
                }
            }

            if (matched) {
                Log.d("TapMute", "Allowed: Keyword found in LATEST content")
                return // ALLOWED
            }
        }

        // 4. App-specific Check (If no keyword in latest content)
        if (!mutePrefs.isMuted(packageName)) return
        
        // 5. Block Policy
        if (mutePrefs.isGlobalMuteEnabled()) {
            cancelNotification(sbn.key)
            mutePrefs.incrementMuteCount(packageName)
            Log.d("TapMute", "Blocked: No keyword in latest content ($packageName)")
        }
    }

    private fun isScreenOn(): Boolean {
        val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isInteractive
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed
    }
}
