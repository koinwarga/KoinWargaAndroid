package com.koinwarga.android.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.koinwarga.android.datasources.local_database.LocalDatabase
import com.koinwarga.android.helpers.Crypto
import com.koinwarga.android.models.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.stellar.sdk.KeyPair
import kotlin.coroutines.resume

class RepositoryImpl(
    private val context: Context,
    private val scope: CoroutineScope
) : RepositoryMockImpl() {

    private val stellar_url = "https://horizon-testnet.stellar.org"
    private val issuerAccountId = "GDRQZSLQX76KRLDXUJT6QEUMP4TTV5ALRTJY32KII2WQHMF4N77QOOQM"

    override suspend fun createNewAccount(accountName: String, pin: String): Boolean {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val newAccountPair = KeyPair.random()

            val encryptedSecretKey = Crypto.encrypt(String(newAccountPair.secretSeed), pin)

            val id = db.accountDao().insert(
                com.koinwarga.android.datasources.local_database.Account(
                    accountId = newAccountPair.accountId,
                    secretKey = encryptedSecretKey,
                    accountName = accountName
                )
            )

            db.close()

            return@withContext true
        }
    }

    override suspend fun getActiveAccount(): Account {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val dbAccount = suspendCancellableCoroutine<com.koinwarga.android.datasources.local_database.Account> { resolve ->
                db.accountDao().getActiveAccount().observeForever {
                    resolve.resume(it)
                }
            }

            return@withContext dbAccount.let {
                Account(
                    id = it.id ?: 0,
                    accountId = it.accountId,
                    secretKey = it.secretKey,
                    accountName = it.accountName,
                    xlm = it.xlm,
                    idr = it.idr,
                    lastPagingToken = it.lastPagingToken
                )
            }
        }
    }

    override suspend fun getActiveAccountLiveData(): LiveData<Account> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountLiveData = MediatorLiveData<Account>()

            accountLiveData.addSource(db.accountDao().getActiveAccount()) {
                accountLiveData.value = Account(
                    id = it.id ?: 0,
                    accountId = it.accountId,
                    secretKey = it.secretKey,
                    accountName = it.accountName,
                    xlm = it.xlm,
                    idr = it.idr,
                    lastPagingToken = it.lastPagingToken
                )
            }

            return@withContext accountLiveData

//            if (dbAccountLiveData == null) {
//                db.close()
//                return@withContext Response.Error<LiveData<Account>>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = "Tidak ada akun")
//            }
//
//            db.close()
//
//            val resultLiveData = MutableLiveData<Account>()
//            resultLiveData.postValue(Account(
//                id = dbAccountLiveData.value?.id ?: 0,
//                accountId = dbAccountLiveData.value?.accountId ?: "",
//                secretKey = dbAccountLiveData.value?.secretKey ?: "",
//                accountName = dbAccountLiveData.value?.accountName ?: "",
//                xlm = dbAccountLiveData.value?.xlm,
//                idr = dbAccountLiveData.value?.idr,
//                lastPagingToken = dbAccountLiveData.value?.lastPagingToken
//            ))
//
//            return@withContext Response.Success(MutableLiveData<Account>())
        }
    }

    override suspend fun isAccountAvailable(): Boolean {
        return true
    }

}