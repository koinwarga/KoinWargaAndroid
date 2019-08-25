package com.koinwarga.android.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.koinwarga.android.datasources.local_database.LocalDatabase
import com.koinwarga.android.helpers.Crypto
import com.koinwarga.android.models.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.stellar.sdk.*

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

            return@withContext true
        }
    }

    override suspend fun getActiveAccount(): Response<Account> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val dbAccount = db.accountDao().getActiveAccount()
                ?: return@withContext Response.Error<Account>(
                code = Response.ErrorCode.ERROR_EMPTY,
                message = "Tidak ada akun")

            return@withContext dbAccount.let {
                Response.Success(Account(
                    id = it.id ?: 0,
                    accountId = it.accountId,
                    secretKey = it.secretKey,
                    accountName = it.accountName,
                    xlm = it.xlm,
                    idr = it.idr,
                    lastPagingToken = it.lastPagingToken
                ))
            }
        }
    }

    override fun getActiveAccountLiveData(): LiveData<Account> {
        val db = LocalDatabase.connect(context)

        return Transformations.map(db.accountDao().getActiveAccountLiveData()) {
            return@map Account(
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

    override suspend fun isAccountAvailable(): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            db.accountDao().getActiveAccount()
                ?: return@withContext Response.Success(false)

            return@withContext Response.Success(true)
        }
    }

    override suspend fun updateActiveAccount(lastPagingToken: String): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val dbAccount = db.accountDao().getActiveAccount()
                ?: return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")

            val pair = KeyPair.fromAccountId(dbAccount.accountId)

            try {
                val server = Server(stellar_url)
                val serverAccount = server.accounts().account(pair)

                val xlm = serverAccount.balances.firstOrNull { it.assetType == "native" }.let { it?.balance }
                val idr = serverAccount.balances.firstOrNull { it.assetCode == "IDR" }.let { it?.balance }

                val modifiedAccount = dbAccount.copy(
                    xlm = xlm,
                    idr = idr,
                    lastPagingToken = lastPagingToken
                )

                db.accountDao().update(modifiedAccount)

                return@withContext Response.Success(true)
            } catch (e: Exception) {
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_CONNECTION,
                    message = e.message.toString())
            }
        }
    }

    override suspend fun trustIDR(password: String): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountFromDB = db.accountDao().getActiveAccount()
                ?: return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")

            try {
                val accountPair = KeyPair.fromSecretSeed(Crypto.decrypt(accountFromDB.secretKey, password))
                val issuerPair = KeyPair.fromAccountId(issuerAccountId)
                val asset = Asset.createNonNativeAsset("IDR", issuerPair)

                val server = Server(stellar_url)

                val newAccount = server.accounts().account(accountPair)

                val changeTrustTransaction = Transaction.Builder(newAccount, Network.TESTNET)
                    .addOperation(ChangeTrustOperation.Builder(asset, "10000000").build())
                    .setTimeout(180)
                    .build()
                changeTrustTransaction.sign(accountPair)

                server.submitTransaction(changeTrustTransaction)

                if (accountFromDB.lastPagingToken != null) {
                    updateActiveAccount(accountFromDB.lastPagingToken)
                }

                return@withContext Response.Success(true)

            } catch (e: Exception) {
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = e.message.toString())
            }
        }
    }

    override suspend fun send(
        to: String,
        amount: Int,
        password: String,
        isNative: Boolean
    ): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountFromDB = db.accountDao().getActiveAccount()
                ?: return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")

            try {
                val accountPair = KeyPair.fromSecretSeed(Crypto.decrypt(accountFromDB.secretKey, password))
                val toPair = KeyPair.fromAccountId(to)
                val issuerPair = KeyPair.fromAccountId(issuerAccountId)
                val asset = if (isNative) AssetTypeNative() else Asset.createNonNativeAsset("IDR", issuerPair)

                val server = Server(stellar_url)

                val ownServerAccount = server.accounts().account(accountPair)

                val transaction = Transaction.Builder(ownServerAccount, Network.TESTNET)
                    .addOperation(PaymentOperation.Builder(toPair, asset, amount.toString()).build())
                    .setTimeout(180)
                    .build()
                transaction.sign(accountPair)

                server.submitTransaction(transaction)

                return@withContext Response.Success(true)

            } catch (e: Exception) {
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = e.message.toString())
            }
        }
    }

    override suspend fun registeringNewMember(to: String, pin: String): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountFromDB = db.accountDao().getActiveAccount()
                ?: return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")

            try {
                val accountPair = KeyPair.fromSecretSeed(Crypto.decrypt(accountFromDB.secretKey, pin))
                val toPair = KeyPair.fromAccountId(to)

                val server = Server(stellar_url)

                val ownServerAccount = server.accounts().account(accountPair)

                val transaction = Transaction.Builder(ownServerAccount, Network.TESTNET)
                    .addOperation(CreateAccountOperation.Builder(toPair, "5").build())
                    .setTimeout(180)
                    .build()
                transaction.sign(accountPair)

                server.submitTransaction(transaction)

                return@withContext Response.Success(true)

            } catch (e: Exception) {
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = e.message.toString())
            }
        }
    }
}