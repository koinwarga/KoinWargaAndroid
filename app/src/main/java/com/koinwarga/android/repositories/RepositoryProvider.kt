package com.koinwarga.android.repositories

import android.content.Context
import com.koinwarga.android.repositories.impl.ContactRepository
import kotlinx.coroutines.CoroutineScope

object RepositoryProvider {

    fun repository(context: Context, scope: CoroutineScope): IRepository {
        return RepositoryImpl(context, scope)
    }

    fun contactRepository(context: Context, scope: CoroutineScope): IContactRepository {
        return ContactRepository(context, scope)
    }

}