package com.koinwarga.android.repositories

import android.content.Context
import kotlinx.coroutines.CoroutineScope

object RepositoryProvider {

    fun repository(context: Context, scope: CoroutineScope): IRepository {
        return RepositoryImpl(context, scope)
    }

}