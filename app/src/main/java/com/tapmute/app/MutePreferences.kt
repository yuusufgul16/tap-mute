package com.tapmute.app

import android.content.Context
import android.content.SharedPreferences

class MutePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tapmute_prefs", Context.MODE_PRIVATE)

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

    companion object {
        private const val KEY_MUTED_APPS = "muted_apps"
        private const val KEY_GLOBAL_MUTE = "global_mute_enabled"
        private const val KEY_KEYWORDS = "mute_keywords"
        private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
        private const val KEY_SCHEDULE_START_H = "schedule_start_h"
        private const val KEY_SCHEDULE_START_M = "schedule_start_m"
        private const val KEY_SCHEDULE_END_H = "schedule_end_h"
        private const val KEY_SCHEDULE_END_M = "schedule_end_m"
        private const val KEY_DASHBOARD_APPS = "dashboard_apps"
        private const val PREF_STATS_PREFIX = "stats_"
        private const val KEY_THEME = "app_theme"
    }

    // --- Keywords ---
    fun addKeyword(word: String) {
        val keywords = getKeywords().toMutableSet()
        keywords.add(word.lowercase())
        prefs.edit().putStringSet(KEY_KEYWORDS, keywords).apply()
    }

    fun removeKeyword(word: String) {
        val keywords = getKeywords().toMutableSet()
        keywords.remove(word.lowercase())
        prefs.edit().putStringSet(KEY_KEYWORDS, keywords).apply()
    }

    fun getKeywords(): Set<String> {
        return prefs.getStringSet(KEY_KEYWORDS, emptySet()) ?: emptySet()
    }

    // --- Statistics ---
    fun incrementMuteCount(packageName: String) {
        val current = getMuteCount(packageName)
        prefs.edit().putInt(PREF_STATS_PREFIX + packageName, current + 1).apply()
    }

    fun getMuteCount(packageName: String): Int {
        return prefs.getInt(PREF_STATS_PREFIX + packageName, 0)
    }

    fun getTotalMuteCount(): Int {
        return prefs.all.filterKeys { it.startsWith(PREF_STATS_PREFIX) }
            .values.filterIsInstance<Int>().sum()
    }

    // --- Schedule ---
    fun setScheduleEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, enabled).apply()
    }

    fun isScheduleEnabled(): Boolean = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)

    fun setSchedule(startH: Int, startM: Int, endH: Int, endM: Int) {
        prefs.edit()
            .putInt(KEY_SCHEDULE_START_H, startH)
            .putInt(KEY_SCHEDULE_START_M, startM)
            .putInt(KEY_SCHEDULE_END_H, endH)
            .putInt(KEY_SCHEDULE_END_M, endM)
            .apply()
    }

    fun getSchedule(): IntArray {
        return intArrayOf(
            prefs.getInt(KEY_SCHEDULE_START_H, 0),
            prefs.getInt(KEY_SCHEDULE_START_M, 0),
            prefs.getInt(KEY_SCHEDULE_END_H, 23),
            prefs.getInt(KEY_SCHEDULE_END_M, 59)
        )
    }

    fun isGlobalMuteEnabled(): Boolean {
        return prefs.getBoolean(KEY_GLOBAL_MUTE, false)
    }

    fun setGlobalMuteEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLOBAL_MUTE, enabled).apply()
    }

    // --- Dashboard Apps (Mockup Redesign) ---
    fun getDashboardApps(): Set<String> {
        // Default to WhatsApp and Instagram for demonstration if empty
        return prefs.getStringSet(KEY_DASHBOARD_APPS, setOf("com.whatsapp", "com.instagram.android")) 
            ?: setOf("com.whatsapp", "com.instagram.android")
    }

    fun addToDashboard(packageName: String) {
        val apps = getDashboardApps().toMutableSet()
        apps.add(packageName)
        prefs.edit().putStringSet(KEY_DASHBOARD_APPS, apps).apply()
    }

    fun removeFromDashboard(packageName: String) {
        val apps = getDashboardApps().toMutableSet()
        apps.remove(packageName)
        prefs.edit().putStringSet(KEY_DASHBOARD_APPS, apps).apply()
    }

    fun getAppTheme(): Int = prefs.getInt(KEY_THEME, -1) // -1 is Follow System

    fun setAppTheme(theme: Int) {
        prefs.edit().putInt(KEY_THEME, theme).apply()
    }
}
