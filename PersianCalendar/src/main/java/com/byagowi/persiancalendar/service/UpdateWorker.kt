package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters

import com.byagowi.persiancalendar.utils.UpdateUtils
import com.byagowi.persiancalendar.utils.Utils

class UpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        Utils.setChangeDateWorker()
        Utils.updateStoredPreference(applicationContext)
        UpdateUtils.update(applicationContext, true)
        return ListenableWorker.Result.success()
    }
}
