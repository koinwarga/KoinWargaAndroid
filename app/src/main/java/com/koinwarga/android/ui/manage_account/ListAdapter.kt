package com.koinwarga.android.ui.manage_account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koinwarga.android.R
import com.koinwarga.android.models.Account

class ListAdapter(private val accounts: List<Account>) : RecyclerView.Adapter<ItemViewHolder>() {

    var setOnAccountSelected: ((account: Account) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.activity_manage_account_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.load(accounts[position])
        holder.itemView.setOnClickListener {
            setOnAccountSelected?.invoke(accounts[position])
        }
    }
}