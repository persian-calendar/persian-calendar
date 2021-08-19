package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.setChangeDateWorker
import com.byagowi.persiancalendar.utils.update
import com.byagowi.persiancalendar.utils.updateStoredPreference
import kotlinx.coroutines.coroutineScope

class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            setChangeDateWorker(applicationContext)
            updateStoredPreference(applicationContext)
            update(applicationContext, true)
        }.onFailure(logException)
        Result.success()
    }
}
