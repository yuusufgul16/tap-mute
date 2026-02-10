package com.tapmute.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch

class ManageAppsAdapter(
    private var apps: List<AppInfo>,
    private val onDashboardToggle: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<ManageAppsAdapter.ViewHolder>() {

    private var filteredApps: List<AppInfo> = apps

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.dashboardCheckbox)
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val status: TextView = view.findViewById(R.id.appStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filteredApps[position]
        val context = holder.itemView.context
        
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.appName
        
        holder.status.text = if (app.isAtDashboard) "Dashboard'da Aktif" else "Listeye ekle"
        holder.status.setTextColor(if (app.isAtDashboard) context.getColor(R.color.neon_teal) else context.getColor(R.color.text_secondary))

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = app.isAtDashboard
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            app.isAtDashboard = isChecked
            onDashboardToggle(app, isChecked)
            holder.status.text = if (isChecked) "Dashboard'da Aktif" else "Listeye ekle"
            holder.status.setTextColor(if (isChecked) context.getColor(R.color.neon_teal) else context.getColor(R.color.text_secondary))
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
