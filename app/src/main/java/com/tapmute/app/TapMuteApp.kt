package com.tapmute.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class TapMuteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = MutePreferences(this)
        val theme = prefs.getAppTheme()
        if (theme != -1) {
            AppCompatDelegate.setDefaultNightMode(theme)
        }
    }
}
