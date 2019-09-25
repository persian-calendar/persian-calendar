package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.utils.UpdateUtils
import com.byagowi.persiancalendar.utils.Utils

class UpdateWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Utils.setChangeDateWorker(context)
        Utils.updateStoredPreference(applicationContext)
        UpdateUtils.update(applicationContext, true)
        return Result.success()
    }
}
