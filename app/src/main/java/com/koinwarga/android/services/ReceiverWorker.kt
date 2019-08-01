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
import com.koinwarga.android.datasources.local_database.LocalDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.stellar.sdk.*
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.operations.ChangeTrustOperationResponse
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional


class ReceiverWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("test", "Start Payment Worker")

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

        val paymentsRequest = server.payments().forAccount(account)

        val lastToken = activeAccount.lastPagingToken
        if (lastToken != null) {
            paymentsRequest.cursor(lastToken)
        }

        paymentsRequest.stream(object : EventListener<OperationResponse> {
            override fun onEvent(payment: OperationResponse) {
                Log.d("test", """Update last paging token ${payment.pagingToken}""")

                activeAccount.lastPagingToken = payment.pagingToken
                db.accountDao().update(activeAccount)

                if (payment is CreateAccountOperationResponse) {

                    val output = """Akun kamu sudah aktif dengan saldo XLM ${payment.startingBalance}"""

                    makeNotification(output)

                } else if (payment is PaymentOperationResponse) {

                    val amount = payment.amount

                    val asset = payment.asset
                    val assetName = if (asset == AssetTypeNative()) {
                        "lumens"
                    } else {
                        """${(asset as AssetTypeCreditAlphaNum).code} : ${asset.issuer.accountId}"""
                    }

                    val output = """$amount $assetName from ${payment.from.accountId}"""

                    Log.d("test", output)

                    makeNotification(output)
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