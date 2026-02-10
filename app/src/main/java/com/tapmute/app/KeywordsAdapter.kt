package com.tapmute.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KeywordsAdapter(
    private var keywords: MutableList<String>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<KeywordsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val keywordText: TextView = view.findViewById(R.id.keywordText)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keyword, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = keywords[position]
        holder.keywordText.text = word
        holder.deleteButton.setOnClickListener {
            onDelete(word)
        }
    }

    override fun getItemCount() = keywords.size

    fun updateKeywords(newKeywords: List<String>) {
        keywords.clear()
        keywords.addAll(newKeywords)
        notifyDataSetChanged()
    }
}
