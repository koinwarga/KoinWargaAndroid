package com.koinwarga.android.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.models.Account
import com.koinwarga.android.models.History
import kotlinx.coroutines.delay

open class RepositoryMockImpl : IRepository {
    override suspend fun createNewAccount(accountName: String, pin: String): Boolean {
        return false
    }

    override suspend fun isAccountAvailable(): Response<Boolean> {
        return Response.Success(true)
    }

    override suspend fun getActiveAccount(): Response<Account> {
        return Response.Success(Account(
            id = 1,
            accountId = "xxx",
            secretKey = "xxx",
            accountName = "Utama",
            xlm = "0",
            idr = "0",
            lastPagingToken = ""
        ))
    }

    override fun getActiveAccountLiveData(): LiveData<Account> {
        val data = MediatorLiveData<Account>()
        data.postValue(Account(
            id = 1,
            accountId = "xxx",
            secretKey = "xxx",
            accountName = "Utama",
            xlm = "0",
            idr = "0",
            lastPagingToken = ""
        ))
        return data
    }

    override suspend fun activateIDR(): LiveData<Boolean> {
        return MutableLiveData<Boolean>()
    }

    override suspend fun registeringNewMember(to: String, pin: String): Response<Boolean> {
        return Response.Success(true)
    }

    override suspend fun send(to: String, amount: Int, password: String, isNative: Boolean): Response<Boolean> {
        return Response.Success(true)
    }

    override suspend fun getHistoryOfActiveAccount(): LiveData<History> {
        return MutableLiveData<History>()
    }

    override suspend fun getAllAccounts(): LiveData<List<Account>> {
        return MutableLiveData<List<Account>>()
    }

    override suspend fun updateActiveAccount(lastPagingToken: String): Response<Boolean> {
        return Response.Success(true)
    }

    override suspend fun trustIDR(password: String): Response<Boolean> {
        return Response.Success(true)
    }
}