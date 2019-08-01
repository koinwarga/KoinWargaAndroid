package com.koinwarga.android.commons

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.koinwarga.android.helpers.isServiceRunning
import com.koinwarga.android.services.ListenPaymentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isServiceRunning(ListenPaymentService::class.java)) {
            val mServiceIntent = Intent(this, ListenPaymentService::class.java)
            startService(mServiceIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    protected fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    protected fun showDialogMessage(msg: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder
            .setTitle("info")
            .setMessage(msg)
            .show()
    }

}