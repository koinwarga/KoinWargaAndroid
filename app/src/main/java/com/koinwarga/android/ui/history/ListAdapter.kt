package com.koinwarga.android.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koinwarga.android.R
import com.koinwarga.android.models.History

class ListAdapter(private val histories: List<History>) : RecyclerView.Adapter<ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.fragment_history_item, parent, false)

        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.load(histories[position])
    }
}