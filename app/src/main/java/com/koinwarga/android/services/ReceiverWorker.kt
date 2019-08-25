package com.koinwarga.android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.koinwarga.android.R
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.RepositoryProvider
import com.koinwarga.android.repositories.Response
import kotlinx.coroutines.*
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional


class ReceiverWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), CoroutineScope by MainScope() {

    private val repository by lazy { RepositoryProvider.repository(context, this) }

    override fun doWork(): Result {
        loadActiveAccount()

        return runBlocking {
            delay(890000)
            Log.d("test", "Stop Listening Payment. Waiting to Restart")
            return@runBlocking Result.success()
        }
    }

    override fun onStopped() {
        Log.d("test", "Stop Payment Stream")
        super.onStopped()
    }

    private fun loadActiveAccount() {
        launch(Dispatchers.IO) {
            Log.d("test", "Load Active Account")
            when (val response: Response<Account> = repository.getActiveAccount()) {
                is Response.Success -> listenPayment(response.body)
                is Response.Error -> restartWorker()
            }
        }
    }

    private fun listenPayment(activeAccount: Account) {
        Log.d("test", "Start Payment Stream")

        val server = Server("https://horizon-testnet.stellar.org")
        val account = KeyPair.fromAccountId(activeAccount.accountId)

        Log.d("test", """Start Listening account ${account.accountId}""")
        Log.d("test", """Last paging token ${activeAccount.lastPagingToken}""")

        val paymentsRequest = server.payments().forAccount(account)

        val lastToken = activeAccount.lastPagingToken
        if (lastToken != null) {
            paymentsRequest.cursor(lastToken)
        }

        paymentsRequest.stream(object : EventListener<OperationResponse> {
            override fun onEvent(payment: OperationResponse) {
                Log.d("test", """Update last paging token ${payment.pagingToken}""")

                launch(Dispatchers.IO) {
                    repository.updateActiveAccount(payment.pagingToken)
                }

                if (payment is CreateAccountOperationResponse) {

                    val output = """Akun kamu sudah aktif dengan saldo XLM ${payment.startingBalance}"""

                    makeNotification(output)

                } else if (payment is PaymentOperationResponse) {

                    if (payment.to.accountId == account.accountId) {
                        val amount = payment.amount

                        val asset = payment.asset
                        val assetName = if (asset == AssetTypeNative()) {
                            "lumens"
                        } else {
                            """${(asset as AssetTypeCreditAlphaNum).code} : ${asset.issuer.accountId}"""
                        }

                        val output = """Uang masuk Rp$amount"""

                        Log.d("test", output)

                        makeNotification(output)
                    }

                }
            }

            override fun onFailure(p0: Optional<Throwable>?, p1: Optional<Int>?) {
                restartWorker()
            }
        })
    }

    private fun restartWorker() {
        runBlocking {
            Log.d("test", "Restart Payment Stream. Restarting in 10 Sec")
            delay(10000)
            loadActiveAccount()
            return@runBlocking
        }
    }

    private fun makeNotification(msg: String) {
        val builder = NotificationCompat.Builder(context, "koinwarga")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Payment")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "koinwarga"
            val descriptionText = "koin dari kita, untuk kita, oleh kita"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("koinwarga", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId("koinwarga")
        }

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }
}