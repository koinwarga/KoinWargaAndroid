package com.koinwarga.android.commons

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseFragment : Fragment(), CoroutineScope by MainScope() {

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    protected fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    protected fun showDialogMessage(msg: String) {
        val alertDialogBuilder = AlertDialog.Builder(context!!)
        alertDialogBuilder
            .setTitle("info")
            .setMessage(msg)
            .show()
    }

}