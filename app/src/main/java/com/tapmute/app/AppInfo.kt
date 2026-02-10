package com.tapmute.app

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    var isMuted: Boolean = false,
    var isAtDashboard: Boolean = false
)
