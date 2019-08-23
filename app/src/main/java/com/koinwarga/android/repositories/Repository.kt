//package com.koinwarga.android.repositories
//
//import android.content.Context
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.koinwarga.android.datasources.local_database.LocalDatabase
//import com.koinwarga.android.helpers.Crypto
//import com.koinwarga.android.models.Account
//import com.koinwarga.android.models.History
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.stellar.sdk.*
//import org.stellar.sdk.requests.RequestBuilder
//import org.stellar.sdk.responses.effects.AccountCreatedEffectResponse
//import org.stellar.sdk.responses.effects.AccountCreditedEffectResponse
//import org.stellar.sdk.responses.effects.AccountDebitedEffectResponse
//
//
//class Repository(
//    private val context: Context,
//    private val scope: CoroutineScope
//) {
//
//    private val stellar_url = "https://horizon-testnet.stellar.org"
//    private val issuerAccountId = "GDRQZSLQX76KRLDXUJT6QEUMP4TTV5ALRTJY32KII2WQHMF4N77QOOQM"
//
//    suspend fun createAccount(
//        accountName: String,
//        password: String,
//        asDefault: Boolean = false
//    ): Response<Account> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val newAccountPair = KeyPair.random()
//
//            val encryptedSecretKey = Crypto.encrypt(String(newAccountPair.secretSeed), password)
//
//            val id = db.accountDao().insert(
//                com.koinwarga.android.datasources.local_database.Account(
//                    accountId = newAccountPair.accountId,
//                    secretKey = encryptedSecretKey,
//                    accountName = accountName,
//                    isDefault = asDefault
//                )
//            )
//
//            val account = db.accountDao().getAccountById(id.toInt()).let {
//                Account(
//                    id = it.id ?: 0,
//                    accountId = it.accountId,
//                    secretKey = it.secretKey,
//                    accountName = it.accountName
//                )
//            }
//
//            db.close()
//
//            return@withContext Response.Success(account)
//        }
//    }
//
//    suspend fun isAccountAvailable(): LiveData<Boolean> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            return@withContext db.accountDao().getAllLiveData().let {
//                return@let MutableLiveData<Boolean>().apply { value = true }
//            }
//        }
//    }
//
//    suspend fun getAccount(): Response<LiveData<Account>> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val dbAccountLiveData = db.accountDao().getDefaultLiveData()
//
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
//        }
//    }
//
//    suspend fun setAccountDefault(account: Account): Response<Account> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val defaultAccountFromDb = db.accountDao().getDefault()
//
//            if (defaultAccountFromDb == null) {
//                db.close()
//                return@withContext Response.Error<Account>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = "Tidak ada akun")
//            }
//
//            val changeDefaultAccountFromDb = defaultAccountFromDb.copy(
//                isDefault = false
//            )
//            val newDefaultAccount = account.let {
//                com.koinwarga.android.datasources.local_database.Account(
//                    id = account.id,
//                    accountId = account.accountId,
//                    secretKey = account.secretKey,
//                    accountName = account.accountName,
//                    isDefault = true,
//                    lastPagingToken = account.lastPagingToken
//                )
//            }
//            db.accountDao().update(changeDefaultAccountFromDb, newDefaultAccount)
//
//            db.close()
//
//            return@withContext Response.Success(Account(
//                id = account.id,
//                accountId = account.accountId,
//                secretKey = account.secretKey,
//                accountName = account.accountName,
//                lastPagingToken = account.lastPagingToken
//            ))
//        }
//    }
//
//    suspend fun getAllAccount(): Response<List<Account>> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val allAccountFromDb = db.accountDao().getAll()
//
//            db.close()
//
//            return@withContext Response.Success(allAccountFromDb.map {
//                Account(
//                    id = it.id ?: -1,
//                    accountId = it.accountId,
//                    secretKey = it.secretKey,
//                    accountName = it.accountName,
//                    lastPagingToken = it.lastPagingToken
//                )
//            })
//        }
//    }
//
////    suspend fun updateAccount(account: Account): Response<Boolean> {
////        return withContext(scope.coroutineContext + Dispatchers.IO) {
////            val db = LocalDatabase.connect(context)
////
////            db.accountDao().update(account.let { com.koinwarga.android.datasources.local_database.Account(
////                id = it.id,
////                accountId = it.accountId,
////                secretKey = it.secretKey,
////                accountName = it.accountName,
////                lastPagingToken = it.lastPagingToken,
////                isDefault = true
////            ) })
////
////            db.close()
////
////            return@withContext Response.Success(true)
////        }
////    }
//
//    suspend fun updateLastPagingToken(account: Account, lastPagingToken: String): Response<Boolean> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            db.accountDao().update(account.let { com.koinwarga.android.datasources.local_database.Account(
//                id = it.id,
//                accountId = it.accountId,
//                secretKey = it.secretKey,
//                accountName = it.accountName,
//                lastPagingToken = lastPagingToken,
//                isDefault = true
//            ) })
//
//            db.close()
//
//            return@withContext Response.Success(true)
//        }
//    }
//
//    suspend fun getAccountDetail(): Response<Account> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val accountFromDB = db.accountDao().getDefault()
//
//            if (accountFromDB == null) {
//                db.close()
//                return@withContext Response.Error<Account>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = "Tidak ada akun")
//            }
//
//            val pair = KeyPair.fromAccountId(accountFromDB.accountId)
//
//            try {
//                val server = Server(stellar_url)
//                val serverAccount = server.accounts().account(pair)
//
//                val xlm = serverAccount.balances.firstOrNull { it.assetType == "native" }.let { it?.balance }
//                val idr = serverAccount.balances.firstOrNull { it.assetCode == "IDR" }.let { it?.balance }
//
//                val account = Account(
//                    id = accountFromDB.id ?: 0,
//                    accountId = accountFromDB.accountId,
//                    secretKey = accountFromDB.secretKey,
//                    accountName = accountFromDB.accountName,
//                    lastPagingToken = accountFromDB.lastPagingToken,
//                    xlm = xlm,
//                    idr = idr
//                )
//
//                return@withContext Response.Success(account)
//            } catch (e: Exception) {
//                return@withContext Response.Error<Account>(
//                    code = Response.ErrorCode.ERROR_CONNECTION,
//                    message = e.message.toString())
//            } finally {
//                db.close()
//            }
//        }
//    }
//
//    suspend fun trustIDR(password: String): Response<Boolean> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val accountFromDB = db.accountDao().getDefault()
//
//            if (accountFromDB == null) {
//                db.close()
//                return@withContext Response.Error<Boolean>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = "Tidak ada akun")
//            }
//
//            try {
//                val accountPair = KeyPair.fromSecretSeed(Crypto.decrypt(accountFromDB.secretKey, password))
//                val issuerPair = KeyPair.fromAccountId(issuerAccountId)
//                val asset = Asset.createNonNativeAsset("IDR", issuerPair)
//
//                val server = Server(stellar_url)
//
//                val newAccount = server.accounts().account(accountPair)
//
//                val changeTrustTransaction = Transaction.Builder(newAccount, Network.TESTNET)
//                    .addOperation(ChangeTrustOperation.Builder(asset, "10000000").build())
//                    .setTimeout(180)
//                    .build()
//                changeTrustTransaction.sign(accountPair)
//
//                server.submitTransaction(changeTrustTransaction)
//
//                return@withContext Response.Success(true)
//
//            } catch (e: Exception) {
//                return@withContext Response.Error<Boolean>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = e.message.toString())
//            } finally {
//                db.close()
//            }
//        }
//    }
//
//    suspend fun send(to: String, amount: Int, password: String, isNative: Boolean = false): Response<Boolean> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val accountFromDB = db.accountDao().getDefault()
//
//            if (accountFromDB == null) {
//                db.close()
//                return@withContext Response.Error<Boolean>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = "Tidak ada akun")
//            }
//
//            try {
//                val accountPair = KeyPair.fromSecretSeed(Crypto.decrypt(accountFromDB.secretKey, password))
//                val toPair = KeyPair.fromAccountId(to)
//                val issuerPair = KeyPair.fromAccountId(issuerAccountId)
//                val asset = if (isNative) AssetTypeNative() else Asset.createNonNativeAsset("IDR", issuerPair)
//
//                val server = Server(stellar_url)
//
//                val ownServerAccount = server.accounts().account(accountPair)
//
//                val transaction = Transaction.Builder(ownServerAccount, Network.TESTNET)
//                    .addOperation(PaymentOperation.Builder(toPair, asset, amount.toString()).build())
//                    .setTimeout(180)
//                    .build()
//                transaction.sign(accountPair)
//
//                server.submitTransaction(transaction)
//
//                return@withContext Response.Success(true)
//
//            } catch (e: Exception) {
//                return@withContext Response.Error<Boolean>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = e.message.toString())
//            }
//        }
//    }
//
//    suspend fun registerAccountToNetwork(to: String, amount: Int, password: String, isNative: Boolean = false): Response<Boolean> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val accountFromDB = db.accountDao().getDefault()
//
//            if (accountFromDB == null) {
//                db.close()
//                return@withContext Response.Error<Boolean>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = "Tidak ada akun")
//            }
//
//            try {
//                val accountPair = KeyPair.fromSecretSeed(Crypto.decrypt(accountFromDB.secretKey, password))
//                val toPair = KeyPair.fromAccountId(to)
//                val issuerPair = KeyPair.fromAccountId(issuerAccountId)
//                val asset = if (isNative) AssetTypeNative() else Asset.createNonNativeAsset("IDR", issuerPair)
//
//                val server = Server(stellar_url)
//
//                val ownServerAccount = server.accounts().account(accountPair)
//
//                val transaction = Transaction.Builder(ownServerAccount, Network.TESTNET)
//                    .addOperation(CreateAccountOperation.Builder(toPair, amount.toString()).build())
//                    .setTimeout(180)
//                    .build()
//                transaction.sign(accountPair)
//
//                server.submitTransaction(transaction)
//
//                return@withContext Response.Success(true)
//
//            } catch (e: Exception) {
//                return@withContext Response.Error<Boolean>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = e.message.toString())
//            }
//        }
//    }
//
//    suspend fun accountHistories(): Response<List<History>> {
//        return withContext(scope.coroutineContext + Dispatchers.IO) {
//            val db = LocalDatabase.connect(context)
//
//            val accountFromDB = db.accountDao().getDefault()
//
//            if (accountFromDB == null) {
//                db.close()
//                return@withContext Response.Error<List<History>>(
//                    code = Response.ErrorCode.ERROR_EMPTY,
//                    message = "Tidak ada akun")
//            }
//
//            val pair = KeyPair.fromAccountId(accountFromDB.accountId)
//
//            try {
//                val server = Server(stellar_url)
//                val accountPair = KeyPair.fromAccountId(accountFromDB.accountId)
//                val effect = server.effects().forAccount(accountPair).order(RequestBuilder.Order.DESC)
//                val response = effect.execute()
//
//                val histories = response.records.map {
//                    when(it) {
//                        is AccountCreatedEffectResponse -> History(it.id, it.type, it.createdAt, true, it.startingBalance)
//                        is AccountCreditedEffectResponse -> History(it.id, it.type, it.createdAt, true, it.amount)
//                        is AccountDebitedEffectResponse -> History(it.id, it.type, it.createdAt, true, it.amount)
//                        else -> History(it.id, it.type, it.createdAt, true, "")
//                    }
//                }
//
//                return@withContext Response.Success(histories)
//            } catch (e: Exception) {
//                return@withContext Response.Error<List<History>>(
//                    code = Response.ErrorCode.ERROR_CONNECTION,
//                    message = e.message.toString())
//            } finally {
//                db.close()
//            }
//        }
//    }
//
//}