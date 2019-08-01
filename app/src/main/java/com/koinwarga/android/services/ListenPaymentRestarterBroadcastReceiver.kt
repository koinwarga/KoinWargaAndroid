package com.koinwarga.android.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ListenPaymentRestarterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, p1: Intent?) {
        Log.d("test", "Restart Listening Payment")
        context?.startService(Intent(context, ListenPaymentService::class.java))
    }

}