package com.koinwarga.android.datasources.local_database

import android.content.Context
import androidx.room.Room

object LocalDatabase {
    fun connect(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "db"
        ).fallbackToDestructiveMigration().build()
    }
}