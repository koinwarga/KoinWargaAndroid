package com.koinwarga.android.repositories

import android.content.Context
import com.koinwarga.android.datasources.local_database.LocalDatabase
import com.koinwarga.android.models.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.stellar.sdk.*
import org.stellar.sdk.AssetTypeNative




class Repository(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private val stellar_url = "https://horizon-testnet.stellar.org"
    private val issuerAccountId = "GDRQZSLQX76KRLDXUJT6QEUMP4TTV5ALRTJY32KII2WQHMF4N77QOOQM"

    suspend fun createAccount(asDefault: Boolean = false): Response<Account> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val newAccountPair = KeyPair.random()
            val newAccount = Account(newAccountPair.accountId, String(newAccountPair.secretSeed))

            db.accountDao().insertAll(
                com.koinwarga.android.datasources.local_database.Account(
                    accountId = newAccount.accountId,
                    secretKey = newAccount.secretKey,
                    isDefault = asDefault
                )
            )

            db.close()

            return@withContext Response.Success(newAccount)
        }
    }

    suspend fun getAccount(): Response<Account> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val account = db.accountDao().getDefault()

            if (account == null) {
                db.close()
                return@withContext Response.Error<Account>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")
            }

            db.close()

            return@withContext Response.Success(Account(account.accountId, account.secretKey))
        }
    }

    suspend fun getAccountDetail(): Response<Account> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountFromDB = db.accountDao().getDefault()

            if (accountFromDB == null) {
                db.close()
                return@withContext Response.Error<Account>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")
            }

            val pair = KeyPair.fromAccountId(accountFromDB.accountId)

            try {
                val server = Server(stellar_url)
                val serverAccount = server.accounts().account(pair)

                val xlm = serverAccount.balances.firstOrNull { it.assetType == "native" }.let { it?.balance }
                val idr = serverAccount.balances.firstOrNull { it.assetCode == "IDR" }.let { it?.balance }

                val account = Account(accountFromDB.accountId, accountFromDB.secretKey, xlm, idr)

                return@withContext Response.Success(account)
            } catch (e: Exception) {
                return@withContext Response.Error<Account>(
                    code = Response.ErrorCode.ERROR_CONNECTION,
                    message = e.message.toString())
            } finally {
                db.close()
            }
        }
    }

    suspend fun trustIDR(): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountFromDB = db.accountDao().getDefault()

            if (accountFromDB == null) {
                db.close()
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")
            }

            try {
                val accountPair = KeyPair.fromSecretSeed(accountFromDB.secretKey)
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

                return@withContext Response.Success(true)

            } catch (e: Exception) {
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = e.message.toString())
            } finally {
                db.close()
            }
        }
    }

    suspend fun send(to: Account, amount: Int, isNative: Boolean = false): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountFromDB = db.accountDao().getDefault()

            if (accountFromDB == null) {
                db.close()
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")
            }

            try {
                val accountPair = KeyPair.fromSecretSeed(accountFromDB.secretKey)
                val toPair = KeyPair.fromAccountId(to.accountId)
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

    suspend fun registerAccountToNetwork(to: Account, amount: Int, isNative: Boolean = false): Response<Boolean> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val accountFromDB = db.accountDao().getDefault()

            if (accountFromDB == null) {
                db.close()
                return@withContext Response.Error<Boolean>(
                    code = Response.ErrorCode.ERROR_EMPTY,
                    message = "Tidak ada akun")
            }

            try {
                val accountPair = KeyPair.fromSecretSeed(accountFromDB.secretKey)
                val toPair = KeyPair.fromAccountId(to.accountId)
                val issuerPair = KeyPair.fromAccountId(issuerAccountId)
                val asset = if (isNative) AssetTypeNative() else Asset.createNonNativeAsset("IDR", issuerPair)

                val server = Server(stellar_url)

                val ownServerAccount = server.accounts().account(accountPair)

                val transaction = Transaction.Builder(ownServerAccount, Network.TESTNET)
                    .addOperation(CreateAccountOperation.Builder(toPair, amount.toString()).build())
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