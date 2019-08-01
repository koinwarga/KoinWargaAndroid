package com.koinwarga.android.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.Repository
import com.koinwarga.android.repositories.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional


class ListenPaymentService : Service(), CoroutineScope by MainScope() {

    private val repository by lazy { Repository(this, this) }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        getCurrentAccount()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        restart()
    }

    private fun restart() {
        val broadcastIntent = Intent(this, ListenPaymentRestarterBroadcastReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    private fun getCurrentAccount() {
        launch(Dispatchers.Main) {
            when(val response = repository.getAccount()) {
                is Response.Success -> startListeningPayment(response.body)
                is Response.Error -> stopSelf()
            }

        }
    }

    private fun updateLastPagingToken(account: Account) {
        launch(Dispatchers.Main) {
            Log.d("test", """update for ${account.id}""")
            when(val response = repository.updateAccount(account)) {
                is Response.Error -> Log.d("test", """Error update lastpagingtoken ${response.message}""")
            }
        }
    }

    private fun startListeningPayment(accountFromDB: Account) {
        Log.d("test", """Start Listening Payment for ${accountFromDB.accountId}""")

        val server = Server("https://horizon-testnet.stellar.org")
        val account = KeyPair.fromAccountId(accountFromDB.accountId)

        val paymentsRequest = server.payments().forAccount(account)

        val lastToken = accountFromDB.lastPagingToken
        Log.d("test", """last paging token $lastToken""")
        if (lastToken != null) {
            paymentsRequest.cursor(lastToken)
        }

        paymentsRequest.stream(object : EventListener<OperationResponse> {
            override fun onEvent(payment: OperationResponse) {
                Log.d("test", """update last paging token ${payment.pagingToken}""")
                accountFromDB.lastPagingToken = payment.pagingToken
                updateLastPagingToken(accountFromDB)

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
    }

    private fun makeNotification(msg: String) {
        Log.d("test", msg)
    }

}