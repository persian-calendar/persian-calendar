package com.byagowi.persiancalendar.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.UPDATE_TAG
import com.byagowi.persiancalendar.service.UpdateWorker
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import java.util.concurrent.TimeUnit

fun startWorker(context: Context) {
    runCatching {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UPDATE_TAG, ExistingPeriodicWorkPolicy.UPDATE,
            // An hourly task to call UpdateWorker.doWork
            PeriodicWorkRequest.Builder(UpdateWorker::class.java, 1L, TimeUnit.HOURS).build()
        )
    }.onFailure(logException).getOrNull().debugAssertNotNull
}
