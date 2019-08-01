package com.koinwarga.android.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ListenPaymentRestarterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, p1: Intent?) {
        Log.i(
            ListenPaymentService::class.java.simpleName,
            "ListenPaymentService Stops!"
        )
        context?.startService(Intent(context, ListenPaymentService::class.java))
    }

}