package com.koinwarga.android.datasources.local_database

import androidx.room.*

@Dao
interface ContactDao {

    @Query("SELECT * FROM Contact")
    fun getAll(): List<Contact>

    @Query("SELECT * FROM Contact WHERE id = :id")
    fun getById(id: Long): Contact

    @Insert
    fun insert(vararg contact: Contact)

    @Insert
    fun insertOne(contact: Contact): Long

    @Update
    fun update(vararg contact: Contact)

    @Delete
    fun delete(vararg contact: Contact)

}