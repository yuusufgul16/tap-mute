package com.tapmute.app

import android.content.Context
import android.content.SharedPreferences

class MutePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tapmute_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MUTED_APPS = "muted_apps"
        private const val KEY_GLOBAL_MUTE = "global_mute_enabled"
    }

    fun addMutedApp(packageName: String) {
        val apps = getMutedApps().toMutableSet()
        apps.add(packageName)
        prefs.edit().putStringSet(KEY_MUTED_APPS, apps).apply()
    }

    fun removeMutedApp(packageName: String) {
        val apps = getMutedApps().toMutableSet()
        apps.remove(packageName)
        prefs.edit().putStringSet(KEY_MUTED_APPS, apps).apply()
    }

    fun isMuted(packageName: String): Boolean {
        return getMutedApps().contains(packageName)
    }

    fun getMutedApps(): Set<String> {
        return prefs.getStringSet(KEY_MUTED_APPS, emptySet()) ?: emptySet()
    }

    fun isGlobalMuteEnabled(): Boolean {
        return prefs.getBoolean(KEY_GLOBAL_MUTE, false)
    }

    fun setGlobalMuteEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLOBAL_MUTE, enabled).apply()
    }
}
