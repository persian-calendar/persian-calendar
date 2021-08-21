package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_TIME
import com.byagowi.persiancalendar.utils.startAthan
import kotlinx.coroutines.coroutineScope

class AlarmWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = coroutineScope {
        val key = inputData.getString(KEY_EXTRA_PRAYER) ?: "FAJR"
        val intendedTime = inputData.getLong(KEY_EXTRA_PRAYER_TIME, 0)
        startAthan(applicationContext, key, intendedTime)
        Result.success()
    }
}
