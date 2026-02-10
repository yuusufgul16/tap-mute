package com.tapmute.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tapmute.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mutePrefs: MutePreferences
    private lateinit var quickToggleAdapter: AppListAdapter // Reusing adapter with custom layout eventually

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mutePrefs = MutePreferences(this)

        setupMasterControl()
        setupDashboardGrid()
        setupNavigation()
        setupSettings()
        
        // Initial state update
        updateUIState()

        // Check notification listener permission
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
        binding.masterButtonContainer.setOnClickListener {
            // Click animation
            val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_click)
            binding.masterButtonContainer.startAnimation(animation)

            val isEnabled = mutePrefs.isGlobalMuteEnabled()
            val newState = !isEnabled
            
            if (newState && !isNotificationListenerEnabled()) {
                showPermissionDialog()
                return@setOnClickListener
            }
            
            mutePrefs.setGlobalMuteEnabled(newState)
            updateUIState()
            
            val msg = if (newState) "Sessiz mod aktif" else "Sessiz mod kapalı"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDashboardGrid() {
        quickToggleAdapter = AppListAdapter(mutableListOf(), isDashboardMode = true) { app, isMuted ->
            if (isMuted) {
                mutePrefs.addMutedApp(app.packageName)
            } else {
                mutePrefs.removeMutedApp(app.packageName)
            }
            updateUIState()
        }
        binding.quickTogglesGrid.layoutManager = GridLayoutManager(this, 2)
        binding.quickTogglesGrid.adapter = quickToggleAdapter
        loadDashboardApps()
    }

    private fun loadDashboardApps() {
        val pm = packageManager
        val dashboardPackageNames = mutePrefs.getDashboardApps()
        val dashboardApps = mutableListOf<AppInfo>()

        for (pkg in dashboardPackageNames) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                val isMuted = mutePrefs.isMuted(pkg)
                dashboardApps.add(AppInfo(appName, pkg, icon, isMuted))
            } catch (e: Exception) {
                // App might have been uninstalled
                Log.e("TapMute", "Dashboard app not found: $pkg")
            }
        }
        quickToggleAdapter.updateApps(dashboardApps)
    }

    private fun setupNavigation() {
        binding.manageAppsButton.setOnClickListener {
            // Intent to ManageAppsActivity (To be created)
            val intent = Intent(this, ManageAppsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSettings() {
        binding.settingsButton.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun updateUIState() {
        val isEnabled = mutePrefs.isGlobalMuteEnabled() && isNotificationListenerEnabled()
        
        if (isEnabled) {
            binding.statusText.text = "Sessiz Mod Aktif"
            binding.statusText.setTextColor(getColor(R.color.neon_teal))
            binding.masterIcon.setImageResource(android.R.drawable.ic_lock_silent_mode)
            binding.masterIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.bg_dark))
            binding.masterButtonContainer.alpha = 1.0f
        } else {
            binding.statusText.text = "Sessiz Mod Kapalı"
            binding.statusText.setTextColor(getColor(R.color.text_secondary))
            binding.masterIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            binding.masterIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.text_secondary))
            binding.masterButtonContainer.alpha = 0.5f // Dimmed look when off
        }
        
        binding.totalBlockedText.text = "${mutePrefs.getTotalMuteCount()} engellendi"
    }

    private fun showSettingsDialog() {
        startActivity(Intent(this, SettingsActivity::class.java))
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
