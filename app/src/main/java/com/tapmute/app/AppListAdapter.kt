package com.tapmute.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch

class AppListAdapter(
    private var apps: List<AppInfo>,
    private val onToggle: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private var filteredApps: List<AppInfo> = apps

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val packageLabel: TextView = view.findViewById(R.id.appPackage)
        val muteSwitch: MaterialSwitch = view.findViewById(R.id.muteSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filteredApps[position]

        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.appName
        holder.packageLabel.text = app.packageName

        // Remove listener before setting checked to avoid triggering callback
        holder.muteSwitch.setOnCheckedChangeListener(null)
        holder.muteSwitch.isChecked = app.isMuted
        holder.muteSwitch.setOnCheckedChangeListener { _, isChecked ->
            app.isMuted = isChecked
            onToggle(app, isChecked)
        }
    }

    override fun getItemCount() = filteredApps.size

    fun filter(query: String) {
        filteredApps = if (query.isEmpty()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        filteredApps = newApps
        notifyDataSetChanged()
    }
}
