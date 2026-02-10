package com.tapmute.app

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tapmute.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mutePrefs: MutePreferences
    private lateinit var dashboardAdapter: AppListAdapter
    private var dashboardApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mutePrefs = MutePreferences(this)

        setupMasterControl()
        setupDashboardGrid()
        setupSettings()
        
        loadDashboardApps()
        updateUIState()

        if (!isNotificationListenerEnabled()) {
            showPermissionDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUIState()
        loadDashboardApps()
    }

    private fun setupMasterControl() {
        binding.masterCard.setOnClickListener {
            val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_click)
            binding.masterCard.startAnimation(animation)

            val currentState = mutePrefs.isGlobalMuteEnabled()
            val newState = !currentState

            if (newState && !isNotificationListenerEnabled()) {
                showPermissionDialog()
                return@setOnClickListener
            }

            mutePrefs.setGlobalMuteEnabled(newState)
            updateUIState()

            val msg = if (newState) "Sessiz Mod Aktif" else "Bildirimler Açık"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDashboardGrid() {
        dashboardAdapter = AppListAdapter(dashboardApps, isDashboardMode = true) { app, isMuted ->
            if (isMuted) {
                mutePrefs.addMutedApp(app.packageName)
            } else {
                mutePrefs.removeMutedApp(app.packageName)
            }
            updateUIState()
        }
        binding.quickTogglesGrid.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        binding.quickTogglesGrid.adapter = dashboardAdapter
    }

    private fun setupSettings() {
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.manageAppsButton.setOnClickListener {
            startActivity(Intent(this, ManageAppsActivity::class.java))
        }
    }

    private fun updateUIState() {
        val enabled = mutePrefs.isGlobalMuteEnabled() && isNotificationListenerEnabled()
        
        if (enabled) {
            binding.statusText.text = "Sessiz Mod Aktif"
            binding.statusText.setTextColor(getColor(R.color.neon_teal))
            binding.masterIcon.setImageResource(android.R.drawable.ic_lock_silent_mode)
            binding.masterIcon.setColorFilter(getColor(R.color.bg_dark))
            binding.masterButtonContainer.setBackgroundResource(R.drawable.bg_master_control)
        } else {
            binding.statusText.text = "Bildirimler Açık"
            binding.statusText.setTextColor(getColor(R.color.text_secondary))
            binding.masterIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            binding.masterIcon.setColorFilter(getColor(R.color.neon_teal))
            binding.masterButtonContainer.setBackgroundResource(R.drawable.bg_card_rounded)
        }

        binding.totalBlockedText.text = "${mutePrefs.getTotalMuteCount()} engellendi"
    }

    private fun loadDashboardApps() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(0)
        val dashboardPackageNames = mutePrefs.getDashboardApps()

        dashboardApps.clear()
        for (appInfo in installedApps) {
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) continue
            if (appInfo.packageName == packageName) continue
            if (!dashboardPackageNames.contains(appInfo.packageName)) continue

            val appName = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            val isMuted = mutePrefs.isMuted(appInfo.packageName)

            dashboardApps.add(AppInfo(appName, appInfo.packageName, icon, isMuted))
        }
        
        dashboardApps.sortBy { it.appName }
        dashboardAdapter.updateApps(dashboardApps)
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, MuteNotificationService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun showPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Bildirim Erişimi Gerekli")
            .setMessage("TapMute'un çalışabilmesi için bildirim erişim izni vermeniz gerekiyor.")
            .setPositiveButton("Ayarlara Git") { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton("Sonra", null)
            .setCancelable(false)
            .show()
    }
}
