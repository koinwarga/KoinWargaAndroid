package com.koinwarga.android.helpers

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE

@Suppress("DEPRECATION") // Deprecated for third party Services.
fun <T> Context.isServiceRunning(service: Class<T>) =
    (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == service.name }