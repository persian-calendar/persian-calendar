package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.setChangeDateWorker
import com.byagowi.persiancalendar.utils.update
import com.byagowi.persiancalendar.utils.updateStoredPreference
import kotlinx.coroutines.coroutineScope

class EmptyWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        Result.success()
    }
}
