package com.koinwarga.android.commons

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {
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