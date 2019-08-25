package com.koinwarga.android.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.koinwarga.android.models.Account
import com.koinwarga.android.models.History

interface IRepository {

    suspend fun createNewAccount(accountName: String, pin: String): Boolean
    suspend fun isAccountAvailable(): Response<Boolean>
    suspend fun getActiveAccount(): Response<Account>
    fun getActiveAccountLiveData(): LiveData<Account>
    suspend fun activateIDR(): LiveData<Boolean>
    suspend fun registeringNewMember(to: String, pin: String): Response<Boolean>
    suspend fun send(to: String, amount: Int, password: String, isNative: Boolean = false): Response<Boolean>
    suspend fun getHistoryOfActiveAccount(): LiveData<History>
    suspend fun getAllAccounts(): LiveData<List<Account>>
    suspend fun updateActiveAccount(lastPagingToken: String): Response<Boolean>
    suspend fun trustIDR(password: String): Response<Boolean>

}