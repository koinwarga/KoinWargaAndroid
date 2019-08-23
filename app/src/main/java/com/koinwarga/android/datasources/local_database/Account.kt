package com.koinwarga.android.datasources.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "account_id", index = true) val accountId: String,
    @ColumnInfo(name = "secret_key") val secretKey: String,
    @ColumnInfo(name = "account_name") val accountName: String,
    @ColumnInfo(name = "xlm") val xlm: String? = null,
    @ColumnInfo(name = "idr") val idr: String? = null,
    @ColumnInfo(name = "last_paging_token") val lastPagingToken: String? = null,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = true
)