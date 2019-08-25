package com.koinwarga.android.datasources.local_database

import android.content.Context
import android.util.Log
import androidx.room.Room

object LocalDatabase {

    private var appDatabase: AppDatabase? = null

    fun connect(context: Context): AppDatabase {
        if(appDatabase == null) {
            Log.d("test", "init db")
            appDatabase = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "db"
            ).fallbackToDestructiveMigration().build()
        } else {
            Log.d("test", "using singleton db")
        }
        return appDatabase as AppDatabase
    }
}