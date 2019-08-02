package com.koinwarga.android.datasources.local_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.koinwarga.android.datasources.local_database.Account

@Dao
interface AccountDao {

    @Query("SELECT * FROM ACCOUNT WHERE is_default IS 1")
    fun getDefault(): Account?

    @Query("SELECT * FROM ACCOUNT")
    fun getAll(): List<Account>

    @Insert
    fun insertAll(vararg accounts: Account)

    @Update
    fun update(vararg accounts: Account)

}