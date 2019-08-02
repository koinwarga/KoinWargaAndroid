package com.koinwarga.android.ui.history

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.koinwarga.android.models.History
import kotlinx.android.synthetic.main.fragment_history_item.view.*

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun load(history: History) {
        with(itemView) {
            txtId.text = history.id
            txtType.text = history.type
            txtCreatedAt.text = history.createdAt

            if (history.amount.isEmpty()) {
                txtAmount.visibility = View.GONE
            } else {
                txtAmount.visibility = View.VISIBLE
                txtAmount.text = history.amount
            }
        }
    }

}