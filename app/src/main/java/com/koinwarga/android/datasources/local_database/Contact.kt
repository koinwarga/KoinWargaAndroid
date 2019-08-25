package com.koinwarga.android.datasources.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "name", index = true) val name: String,
    @ColumnInfo(name = "accound_id") val accountId: String
)