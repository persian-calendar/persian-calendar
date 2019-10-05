package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.utils.UpdateUtils
import com.byagowi.persiancalendar.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
        try {
            Utils.setChangeDateWorker(context)
            Utils.updateStoredPreference(applicationContext)
            UpdateUtils.update(applicationContext, true)
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }

}
