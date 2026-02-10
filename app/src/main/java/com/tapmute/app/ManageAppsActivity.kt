package com.tapmute.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.tapmute.app.databinding.ActivityManageAppsBinding

class ManageAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageAppsBinding
    private lateinit var mutePrefs: MutePreferences
    private lateinit var allAppsAdapter: ManageAppsAdapter
    private lateinit var dashboardAppsAdapter: ManageAppsAdapter
    private var allApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mutePrefs = MutePreferences(this)
        
        setupToolbar()
        setupRecyclerViews()
        setupSearchBar()
        setupApplyButton()
        loadApps()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        dashboardAppsAdapter = ManageAppsAdapter(mutableListOf(), 
            onDashboardToggle = { app, isSelected -> updateDashboardStatus(app, isSelected) }
        )
        binding.dashboardAppsList.layoutManager = LinearLayoutManager(this)
        binding.dashboardAppsList.adapter = dashboardAppsAdapter

        allAppsAdapter = ManageAppsAdapter(mutableListOf(),
            onDashboardToggle = { app, isSelected -> updateDashboardStatus(app, isSelected) }
        )
        binding.allAppsList.layoutManager = LinearLayoutManager(this)
        binding.allAppsList.adapter = allAppsAdapter
    }

    private fun setupSearchBar() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                allAppsAdapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupApplyButton() {
        binding.applyChangesButton.setOnClickListener {
            finish()
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(0)
        val dashboardPackageNames = mutePrefs.getDashboardApps()

        allApps.clear()
        val dashboardList = mutableListOf<AppInfo>()
        val otherList = mutableListOf<AppInfo>()

        for (appInfo in installedApps) {
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) continue
            if (appInfo.packageName == packageName) continue

            val appName = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            val isMuted = mutePrefs.isMuted(appInfo.packageName)
            val appObj = AppInfo(appName, appInfo.packageName, icon, isMuted)

            allApps.add(appObj)
            if (dashboardPackageNames.contains(appInfo.packageName)) {
                dashboardList.add(appObj)
            } else {
                otherList.add(appObj)
            }
        }

        dashboardAppsAdapter.updateApps(dashboardList)
        allAppsAdapter.updateApps(otherList)
    }

    private fun updateDashboardStatus(app: AppInfo, isSelected: Boolean) {
        if (isSelected) {
            mutePrefs.addToDashboard(app.packageName)
        } else {
            mutePrefs.removeFromDashboard(app.packageName)
            // If removed from dashboard, also unmute it to ensure notifications are allowed
            mutePrefs.removeMutedApp(app.packageName)
            app.isMuted = false
        }
        // Refresh lists to move item between categories if needed
        loadApps()
    }
}
