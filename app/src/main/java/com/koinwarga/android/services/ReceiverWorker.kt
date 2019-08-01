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
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional


class ReceiverWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("test", "Start Payment Worker")

        val db = LocalDatabase.connect(context)
        val activeAccount = db.accountDao().getDefault()

        if (activeAccount == null) {
            Log.d("test", "failed")
            return Result.failure()
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

                if (payment is PaymentOperationResponse) {

                    val amount = payment.amount

                    val asset = payment.asset
                    val assetName = if (asset == AssetTypeNative()) {
                        "lumens"
                    } else {
                        """${(asset as AssetTypeCreditAlphaNum).code} : ${asset.issuer.accountId}"""
                    }

                    val output = """$amount $assetName from ${payment.from.accountId}"""

                    makeNotification(output)
                }

            }

            override fun onFailure(p0: Optional<Throwable>?, p1: Optional<Int>?) {
                Log.d("test", "Listening payment failure")
            }
        })

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