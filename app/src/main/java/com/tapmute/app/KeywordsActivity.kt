package com.tapmute.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.tapmute.app.databinding.ActivityKeywordsBinding

class KeywordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeywordsBinding
    private lateinit var mutePrefs: MutePreferences
    private lateinit var adapter: KeywordsAdapter
    
    private val suggestions = listOf("acil", "neredesin", "yavrum", "canım", "doktor", "hastane", "çabuk", "aç şu telefonu")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeywordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mutePrefs = MutePreferences(this)
        
        setupToolbar()
        setupInput()
        setupSuggestions()
        setupRecyclerView()
        loadKeywords()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupInput() {
        binding.addKeywordButton.setOnClickListener {
            val word = binding.keywordInput.text.toString().trim().lowercase()
            if (word.isNotBlank()) {
                mutePrefs.addKeyword(word)
                binding.keywordInput.setText("")
                loadKeywords()
            }
        }
    }

    private fun setupSuggestions() {
        suggestions.forEach { suggestion ->
            val chip = Chip(this).apply {
                text = suggestion
                setTextColor(getColor(R.color.text_primary))
                setChipBackgroundColorResource(R.color.bg_card)
                setOnClickListener {
                    mutePrefs.addKeyword(suggestion)
                    loadKeywords()
                    Toast.makeText(this@KeywordsActivity, "Eklendi: $suggestion", Toast.LENGTH_SHORT).show()
                }
            }
            binding.suggestionGroup.addView(chip)
        }
    }

    private fun setupRecyclerView() {
        adapter = KeywordsAdapter(mutableListOf()) { word ->
            mutePrefs.removeKeyword(word)
            loadKeywords()
        }
        binding.keywordsList.layoutManager = LinearLayoutManager(this)
        binding.keywordsList.adapter = adapter
    }

    private fun loadKeywords() {
        val keywords = mutePrefs.getKeywords().toList().sorted()
        adapter.updateKeywords(keywords)
        binding.activeKeywordsTitle.text = "Aktif Filtreler (${keywords.size})"
    }
}
