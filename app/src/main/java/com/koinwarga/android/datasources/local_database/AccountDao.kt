package com.koinwarga.android.datasources.local_database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.koinwarga.android.datasources.local_database.Account

@Dao
interface AccountDao {

//    @Query("SELECT * FROM ACCOUNT WHERE is_default IS 1")
//    fun getDefault(): Account?

    @Query("SELECT * FROM ACCOUNT WHERE is_default IS 1")
    fun getActiveAccount(): LiveData<Account>

    @Query("SELECT * FROM ACCOUNT")
    fun getAll(): List<Account>

    @Query("SELECT * FROM ACCOUNT")
    fun getAllLiveData(): List<Account>

    @Query("SELECT * FROM ACCOUNT WHERE id = :id")
    fun getAccountById(id: Int): Account

    @Insert
    fun insert(account: Account): Long

    @Update
    fun update(vararg accounts: Account)

    @Update
    fun updateLiveData(account: Account)

}