package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.CHANGE_DATE_TAG
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.coroutineScope
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

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
            it[GregorianCalendar.MILLISECOND] = 0
        }.timeInMillis.milliseconds + 1.days - System.currentTimeMillis().milliseconds
        val dayIsChangedWorker = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setInitialDelay(remainedMillis.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(CHANGE_DATE_TAG, ExistingWorkPolicy.REPLACE, dayIsChangedWorker)
            .enqueue()
    }
}
