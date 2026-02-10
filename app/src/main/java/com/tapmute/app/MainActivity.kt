package com.tapmute.app

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tapmute.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mutePrefs: MutePreferences
    private lateinit var adapter: AppListAdapter
    private var appList = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mutePrefs = MutePreferences(this)

        setupRecyclerView()
        setupSearch()
        setupGlobalToggle()
        setupSettings()
        loadInstalledApps()

        // Check notification listener permission
        if (!isNotificationListenerEnabled()) {
            showPermissionDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh permission state
        updateGlobalToggleState()
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter(appList) { app, isMuted ->
            if (isMuted) {
                mutePrefs.addMutedApp(app.packageName)
            } else {
                mutePrefs.removeMutedApp(app.packageName)
            }
            updateMutedCount()
        }
        binding.appList.layoutManager = LinearLayoutManager(this)
        binding.appList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupGlobalToggle() {
        binding.globalMuteSwitch.isChecked = mutePrefs.isGlobalMuteEnabled()
        binding.globalMuteSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isNotificationListenerEnabled()) {
                binding.globalMuteSwitch.isChecked = false
                showPermissionDialog()
                return@setOnCheckedChangeListener
            }
            mutePrefs.setGlobalMuteEnabled(isChecked)
            updateGlobalToggleState()
        }
        updateGlobalToggleState()
    }

    private fun setupSettings() {
        binding.settingsButton.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        val items = arrayOf("Kelime Filtresi (Smart Filter)")
        MaterialAlertDialogBuilder(this)
            .setTitle("Gelişmiş Ayarlar")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showKeywordsDialog()
                }
            }
            .setPositiveButton("Kapat", null)
            .show()
    }

    private fun showKeywordsDialog() {
        val keywords = mutePrefs.getKeywords().toMutableList()
        val suggestions = listOf("acil", "neredesin", "yavrum", "canım", "doktor", "hastane", "çabuk", "aç şu telefonu")
        
        // Building a string to show current keywords
        val currentText = if (keywords.isEmpty()) "Henüz kelime eklenmedi." else keywords.joinToString(", ")
        
        val input = android.widget.EditText(this).apply {
            hint = "Kelime ekle (örn: acil)"
            setSingleLine(true)
        }

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 20, 60, 20)
            addView(input)
            
            // Current keywords description
            addView(android.widget.TextView(this@MainActivity).apply {
                text = "\nAktif Filtreler:\n$currentText"
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
            })

            // Suggestions title
            addView(android.widget.TextView(this@MainActivity).apply {
                text = "\nÖneriler (Tıkla ve Ekle):"
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.text_primary))
            })

            // Flow layout for suggestions (using a simple LinearLayout wrapping for now)
            val suggestionsLayout = android.widget.LinearLayout(this@MainActivity).apply {
                orientation = android.widget.LinearLayout.VERTICAL
            }
            
            suggestions.chunked(3).forEach { rowWords ->
                val row = android.widget.LinearLayout(this@MainActivity).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                }
                rowWords.forEach { word ->
                    val chip = com.google.android.material.button.MaterialButton(this@MainActivity, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                        text = word
                        textSize = 10f
                        layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            setMargins(4, 4, 4, 4)
                        }
                        setOnClickListener {
                            mutePrefs.addKeyword(word)
                            Toast.makeText(this@MainActivity, "Eklendi: $word", Toast.LENGTH_SHORT).show()
                            // Refresh dialog or close
                        }
                    }
                    row.addView(chip)
                }
                suggestionsLayout.addView(row)
            }
            addView(suggestionsLayout)
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Kelime Filtresi (Smart Filter)")
            .setMessage("Büyük/küçük harf duyarsızdır (Acil = acil). Bu kelimeleri içeren bildirimlere izin verilir.")
            .setView(container)
            .setPositiveButton("Ekle") { _, _ ->
                val word = input.text.toString().trim().lowercase()
                if (word.isNotBlank()) {
                    mutePrefs.addKeyword(word)
                    Toast.makeText(this, "Eklendi: $word", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Temizle") { _, _ ->
                keywords.forEach { mutePrefs.removeKeyword(it) }
                Toast.makeText(this, "Filtre temizlendi", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Kapat", null)
            .show()
    }

    private fun updateGlobalToggleState() {
        val enabled = mutePrefs.isGlobalMuteEnabled() && isNotificationListenerEnabled()
        binding.globalMuteSwitch.isChecked = enabled

        if (enabled) {
            binding.statusText.text = "🔇 Sessiz mod aktif"
            binding.statusCard.setCardBackgroundColor(getColor(R.color.status_active))
        } else {
            binding.statusText.text = "🔔 Bildirimler açık"
            binding.statusCard.setCardBackgroundColor(getColor(R.color.status_inactive))
        }
        updateMutedCount()
    }

    private fun updateMutedCount() {
        val count = mutePrefs.getMutedApps().size
        binding.mutedCountText.text = "$count uygulama seçili"
        binding.totalBlockedText.text = "${mutePrefs.getTotalMuteCount()} engellendi"
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(0)

        appList.clear()
        for (appInfo in installedApps) {
            // Skip system apps and self
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) continue
            if (appInfo.packageName == packageName) continue

            val appName = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            val isMuted = mutePrefs.isMuted(appInfo.packageName)

            appList.add(AppInfo(appName, appInfo.packageName, icon, isMuted))
        }

        // Sort: muted first, then alphabetical
        appList.sortWith(compareByDescending<AppInfo> { it.isMuted }.thenBy { it.appName })

        adapter.updateApps(appList)
        updateMutedCount()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, MuteNotificationService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun showPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Bildirim Erişimi Gerekli")
            .setMessage("TapMute'un bildirimleri engelleyebilmesi için bildirim erişim izni vermeniz gerekiyor.\n\nAyarlar > TapMute > İzin Ver")
            .setPositiveButton("Ayarlara Git") { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton("Sonra") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "İzin verilmeden uygulama çalışmaz", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }
}
