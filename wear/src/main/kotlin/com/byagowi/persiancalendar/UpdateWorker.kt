package com.byagowi.persiancalendar

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        applicationContext.requestComplicationUpdate()
        applicationContext.requestTileUpdate()
        return Result.success()
    }
}
