package com.koinwarga.android.repositories

import androidx.lifecycle.LiveData
import com.koinwarga.android.models.Account
import com.koinwarga.android.models.History

interface IRepository {

    suspend fun createNewAccount(accountName: String, pin: String): Boolean
    suspend fun isAccountAvailable(): Boolean
    suspend fun getActiveAccount(): Account
    suspend fun getActiveAccountLiveData(): LiveData<Account>
    suspend fun activateIDR(): LiveData<Boolean>
    suspend fun registeringNewMember(): LiveData<Boolean>
    suspend fun send(): LiveData<Boolean>
    suspend fun getHistoryOfActiveAccount(): LiveData<History>
    suspend fun getAllAccounts(): LiveData<List<Account>>

}