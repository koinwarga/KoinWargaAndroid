package com.koinwarga.android.ui.scanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import kotlinx.android.synthetic.main.activity_scanner.*

class ScannerActivity : BaseActivity() {

    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        codeScanner = CodeScanner(this, vScanner)

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val intent = Intent()
                intent.putExtra("accountId", it.text)
                setResult(200, intent)
                finish()
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
