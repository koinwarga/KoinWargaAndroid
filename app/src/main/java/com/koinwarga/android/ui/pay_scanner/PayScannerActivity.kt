package com.koinwarga.android.ui.pay_scanner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import com.koinwarga.android.ui.pay_preview.PayPreviewActivity
import kotlinx.android.synthetic.main.activity_scanner.*

class PayScannerActivity : BaseActivity() {

    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_scanner)

        codeScanner = CodeScanner(this, vScanner)

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Intent(this, PayPreviewActivity::class.java).apply {
                    putExtra("trx_envelop", it.text)
                    startActivity(this)
                }
            }
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}
