package com.koinwarga.android

import androidx.multidex.MultiDexApplication
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.koinwarga.android.services.ReceiverWorker
import java.util.concurrent.TimeUnit

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).cancelAllWork()
        val receiverWorkRequest = PeriodicWorkRequestBuilder<ReceiverWorker>(
            15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueue(receiverWorkRequest)
    }

}