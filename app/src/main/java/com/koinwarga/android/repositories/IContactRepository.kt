package com.koinwarga.android.repositories

import com.koinwarga.android.models.Contact

interface IContactRepository {
    suspend fun getAllContact(): Response<List<Contact>>
    suspend fun updateContact(contact: Contact): Response<Boolean>
    suspend fun deleteContact(contact: Contact): Response<Boolean>
}