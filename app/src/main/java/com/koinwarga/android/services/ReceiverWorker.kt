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
import com.koinwarga.android.datasources.local_database.AppDatabase
import com.koinwarga.android.datasources.local_database.LocalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional


class ReceiverWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

//    private val repository by lazy { Repository(context, this) }

    override fun doWork(): Result {
        listenPayment()

        return runBlocking {
            delay(890000)
            Log.d("test", "Finish listening payment from worker")
            return@runBlocking Result.success()
        }
    }

    override fun onStopped() {
        Log.d("test", "Stop Payment Worker")
        super.onStopped()
    }

    private fun listenPayment() {
        Log.d("test", "Start Payment Worker")
        val db = LocalDatabase.connect(context)
        val activeAccount = db.accountDao().getDefault() ?: return runBlocking {
            Log.d("test", "failed")
            delay(10000)
            Log.d("test", "Restart payment stream")
            listenPayment()
            return@runBlocking
        }

        val server = Server("https://horizon-testnet.stellar.org")
        val account = KeyPair.fromAccountId(activeAccount.accountId)
        Log.d("test", """Listen payment ${activeAccount.accountId}""")

        val paymentsRequest = server.payments().forAccount(account)

        val lastToken = activeAccount.lastPagingToken
        if (lastToken != null) {
            paymentsRequest.cursor(lastToken)
        }

        paymentsRequest.stream(object : EventListener<OperationResponse> {
            override fun onEvent(payment: OperationResponse) {
                Log.d("test", """Update last paging token ${payment.pagingToken}""")

                activeAccount.lastPagingToken = payment.pagingToken
                if (activeAccount.idr == null) {
                    activeAccount.idr = "0"
                }
                activeAccount.idr = (activeAccount.idr?.toFloat()?.plus(1000)).toString()
                db.accountDao().update(activeAccount)

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
                Log.d("test", "Listening payment failure")

                runBlocking {
                    delay(10000)
                    Log.d("test", "Restart payment stream")
                    listenPayment()
                }
            }
        })
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