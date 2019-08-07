package com.koinwarga.android.ui.pay_preview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.koinwarga.android.R
import kotlinx.android.synthetic.main.activity_pay_preview.*
import org.stellar.sdk.Network
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Server
import org.stellar.sdk.Transaction


class PayPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_preview)

        val trxEnvelop = intent.getStringExtra("trx_envelop")

        val tx = Transaction.fromEnvelopeXdr(trxEnvelop, Network.TESTNET)

        txtFrom.text = (tx.operations[0] as PaymentOperation).destination.accountId
        txtAmount.text = (tx.operations[0] as PaymentOperation).amount

        Log.d("test", trxEnvelop)
    }
}
