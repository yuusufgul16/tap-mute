package com.tapmute.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.tapmute.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: MutePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = MutePreferences(this)

        setupToolbar()
        setupListeners()
        updateThemeText()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.keywordsSetting.setOnClickListener {
            startActivity(Intent(this, KeywordsActivity::class.java))
        }

        binding.themeSetting.setOnClickListener {
            showThemeSelectionDialog()
        }

        binding.notificationAccessSetting.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun updateThemeText() {
        val theme = prefs.getAppTheme()
        binding.currentThemeText.text = when (theme) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO -> "Aydınlık"
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES -> "Karanlık"
            else -> "Sistem Varsayılanı"
        }
    }

    private fun showThemeSelectionDialog() {
        val options = arrayOf("Aydınlık", "Karanlık", "Sistem Varsayılanı")
        val values = intArrayOf(
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO,
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES,
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        val currentTheme = prefs.getAppTheme()
        val checkedItem = when (currentTheme) {
            values[0] -> 0
            values[1] -> 1
            else -> 2
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Tema Seçin")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val selectedTheme = values[which]
                prefs.setAppTheme(selectedTheme)
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(selectedTheme)
                updateThemeText()
                dialog.dismiss()
            }
            .show()
    }
}
