package com.koinwarga.android.datasources.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "secret_key") val secretKey: String,
    @ColumnInfo(name = "last_paging_token") var lastPagingToken: String? = null,
    @ColumnInfo(name = "is_default") val isDefault: Boolean
)