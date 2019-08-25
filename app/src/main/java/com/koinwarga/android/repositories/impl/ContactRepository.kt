package com.koinwarga.android.repositories.impl

import android.content.Context
import com.koinwarga.android.datasources.local_database.LocalDatabase
import com.koinwarga.android.models.Contact
import com.koinwarga.android.repositories.IContactRepository
import com.koinwarga.android.repositories.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepository(
    private val context: Context,
    private val scope: CoroutineScope
) : IContactRepository {

    override suspend fun getAllContact(): Response<List<Contact>> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val dbContacts = db.contactDao().getAll()

            val contacts = dbContacts.map {
                Contact(
                    id = it.id ?: 0,
                    name = it.name,
                    accountId = it.accountId
                )
            }

            return@withContext Response.Success(contacts)
        }
    }

    override suspend fun updateContact(contact: Contact): Response<Boolean> {
        val db = LocalDatabase.connect(context)

        val dbContact = db.contactDao().getById(contact.id.toLong())
        val modifiedContact = dbContact.copy(
            name = contact.name,
            accountId = contact.accountId
        )
        db.contactDao().update(modifiedContact)

        return Response.Success(true)
    }

    override suspend fun deleteContact(contact: Contact): Response<Boolean> {
        val db = LocalDatabase.connect(context)

        val dbContact = db.contactDao().getById(contact.id.toLong())
        db.contactDao().delete(dbContact)

        return Response.Success(true)
    }
}