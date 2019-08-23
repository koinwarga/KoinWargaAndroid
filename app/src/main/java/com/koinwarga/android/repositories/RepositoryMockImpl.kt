package com.koinwarga.android.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.models.Account
import com.koinwarga.android.models.History
import kotlinx.coroutines.delay

open class RepositoryMockImpl : IRepository {
    override suspend fun createNewAccount(accountName: String, pin: String): Boolean {
        return true
    }

    override suspend fun isAccountAvailable(): Boolean {
        return false
    }

    override suspend fun getActiveAccount(): Account {
        return Account(
            id = 1,
            accountId = "xxx",
            secretKey = "xxx",
            accountName = "Utama",
            xlm = "0",
            idr = "0",
            lastPagingToken = ""
        )
    }

    override suspend fun getActiveAccountLiveData(): LiveData<Account> {
        val data = MutableLiveData<Account>()
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

    override suspend fun registeringNewMember(): LiveData<Boolean> {
        return MutableLiveData<Boolean>()
    }

    override suspend fun send(): LiveData<Boolean> {
        return MutableLiveData<Boolean>()
    }

    override suspend fun getHistoryOfActiveAccount(): LiveData<History> {
        return MutableLiveData<History>()
    }

    override suspend fun getAllAccounts(): LiveData<List<Account>> {
        return MutableLiveData<List<Account>>()
    }
}