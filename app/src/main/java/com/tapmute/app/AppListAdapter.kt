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
    private val isDashboardMode: Boolean = false,
    private val onToggle: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private var filteredApps: List<AppInfo> = apps

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val muteSwitch: MaterialSwitch = if (view.id == R.id.appMuteSwitch) 
            view as MaterialSwitch 
        else 
            view.findViewById(R.id.appMuteSwitch) ?: view.findViewById(R.id.muteSwitch)
            
        // For standard list mode
        val packageLabel: TextView? = try { view.findViewById(R.id.appPackage) } catch (e: Exception) { null }
        // For dashboard mode
        val statusLabel: TextView? = try { view.findViewById(R.id.appStatus) } catch (e: Exception) { null }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (isDashboardMode) R.layout.item_quick_toggle else R.layout.item_app
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filteredApps[position]

        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.appName
        
        holder.packageLabel?.text = app.packageName
        holder.statusLabel?.text = if (app.isMuted) "SESSİZ" else "AKTİF"
        holder.statusLabel?.setTextColor(
            if (app.isMuted) holder.itemView.context.getColor(R.color.text_secondary) 
            else holder.itemView.context.getColor(R.color.neon_teal)
        )

        holder.muteSwitch.setOnCheckedChangeListener(null)
        holder.muteSwitch.isChecked = app.isMuted
        holder.muteSwitch.setOnCheckedChangeListener { _, isChecked ->
            app.isMuted = isChecked
            holder.statusLabel?.text = if (isChecked) "SESSİZ" else "AKTİF"
            holder.statusLabel?.setTextColor(
                if (isChecked) holder.itemView.context.getColor(R.color.text_secondary) 
                else holder.itemView.context.getColor(R.color.neon_teal)
            )
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
