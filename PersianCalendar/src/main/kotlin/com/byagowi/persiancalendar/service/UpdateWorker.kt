package com.byagowi.persiancalendar.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.CHANGE_DATE_TAG
import com.byagowi.persiancalendar.entities.DeviceEventInterval
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.updateAndWait
import io.github.persiancalendar.calendar.CivilDate
import kotlinx.coroutines.coroutineScope
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            updateStoredPreference(applicationContext)
            updateAndWait(applicationContext, true)
        }.onFailure(logException)
        // Replacing the current day-change work can cancel this worker. Finish its refresh first.
        scheduleDayChangesUpdates()
        Result.success()
    }

    private fun scheduleDayChangesUpdates() {
        val now = GregorianCalendar()
        val remainedMillis = nextDateUpdateMillis(now) - now.timeInMillis
        val dayIsChangedWorker = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setInitialDelay(remainedMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(CHANGE_DATE_TAG, ExistingWorkPolicy.REPLACE, dayIsChangedWorker)
            .enqueue()
    }
}

internal fun nextDateUpdateMillis(now: GregorianCalendar): Long {
    val tomorrow = CivilDate(now.toCivilDate().toJdn() + 1)
    return DeviceEventInterval.startOfDay(tomorrow, now.timeZone).timeInMillis + 1.seconds.inWholeMilliseconds
}
