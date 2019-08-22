package com.koinwarga.android.ui.manage_account

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.koinwarga.android.models.Account
import kotlinx.android.synthetic.main.activity_manage_account_item.view.*

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun load(account: Account) {
        with(itemView) {
            txtName.text = account.accountName
            txtAccount.text = account.accountId
        }
    }
}