package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.CHANGE_DATE_TAG
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.coroutineScope
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            scheduleDayChangesUpdates()
            updateStoredPreference(applicationContext)
            update(applicationContext, true)
        }.onFailure(logException)
        Result.success()
    }

    private fun scheduleDayChangesUpdates() {
        val remainedMillis = GregorianCalendar().also {
            it[GregorianCalendar.HOUR_OF_DAY] = 0
            it[GregorianCalendar.MINUTE] = 0
            it[GregorianCalendar.SECOND] = 1
        }.timeInMillis + DAY_IN_MILLIS - System.currentTimeMillis()
        val dayIsChangedWorker = OneTimeWorkRequest.Builder(UpdateWorker::class.java)
            .setInitialDelay(remainedMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(CHANGE_DATE_TAG, ExistingWorkPolicy.REPLACE, dayIsChangedWorker)
            .enqueue()
    }
}
