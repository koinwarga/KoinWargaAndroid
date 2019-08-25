package com.koinwarga.android.datasources.local_database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.koinwarga.android.datasources.local_database.Account
import com.koinwarga.android.datasources.local_database.AccountDao

@Database(entities = [Account::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun contactDao(): ContactDao
}