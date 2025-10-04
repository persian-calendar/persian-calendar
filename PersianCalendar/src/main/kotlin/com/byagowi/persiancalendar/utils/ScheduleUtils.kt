package com.byagowi.persiancalendar.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import com.byagowi.persiancalendar.UPDATE_TAG
import com.byagowi.persiancalendar.service.UpdateWorker
import java.util.concurrent.TimeUnit

/**
 * Worker utilities — safe helpers around WorkManager to schedule, query and control the
 * periodic UpdateWorker used by the application.
 *
 * Improvements applied:
 * - Defensive error handling (no unchecked exceptions escape)
 * - Minimum period clamping for PeriodicWorkRequest (WorkManager requires >= 15 minutes)
 * - Small, documented API for starting/stopping/rescheduling and querying worker state
 */

private const val MIN_PERIODIC_INTERVAL_MINUTES = 15L

private fun clampIntervalHours(intervalHours: Long): Long {
    val minutes = TimeUnit.HOURS.toMinutes(intervalHours)
    return if (minutes < MIN_PERIODIC_INTERVAL_MINUTES) {
        TimeUnit.MINUTES.toHours(MIN_PERIODIC_INTERVAL_MINUTES)
    } else intervalHours
}

/**
 * Schedule a periodic UpdateWorker with a default hourly interval.
 * Uses ExistingPeriodicWorkPolicy.UPDATE so configuration changes replace the existing request.
 */
fun startWorker(context: Context) {
    startWorkerWithInterval(context, 1L)
}

/**
 * Schedule the periodic worker with a custom interval in hours. The interval will be clamped
 * to WorkManager's minimum allowed period (15 minutes) to avoid IllegalArgumentException.
 */
fun startWorkerWithInterval(context: Context, intervalHours: Long) {
    runCatching {
        val safeIntervalHours = clampIntervalHours(intervalHours)
        val request = PeriodicWorkRequestBuilder<UpdateWorker>(safeIntervalHours, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UPDATE_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }.onFailure { t ->
        logException(t)
    }
}

/**
 * Trigger a one-time immediate execution of UpdateWorker.
 */
fun startOneTimeWorker(context: Context) {
    runCatching {
        val request = OneTimeWorkRequestBuilder<UpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }.onFailure { t ->
        logException(t)
    }
}

/**
 * Cancel the periodic worker (unique work with UPDATE_TAG).
 */
fun cancelWorker(context: Context) {
    runCatching {
        WorkManager.getInstance(context).cancelUniqueWork(UPDATE_TAG)
    }.onFailure { t ->
        logException(t)
    }
}

/**
 * Return true if any enqueued WorkInfo for UPDATE_TAG is currently running.
 */
fun isWorkerRunning(context: Context): Boolean {
    return runCatching {
        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UPDATE_TAG).get()
        workInfos.any { it.state == WorkInfo.State.RUNNING }
    }.getOrElse { e ->
        logException(e)
        false
    }
}

/**
 * Restart the periodic worker with a given interval (default: 1 hour).
 */
fun restartWorker(context: Context, intervalHours: Long = 1L) {
    cancelWorker(context)
    startWorkerWithInterval(context, intervalHours)
}

/**
 * Return the current (first) WorkInfo.State for the unique work, or null if unavailable.
 */
fun getWorkerState(context: Context): WorkInfo.State? {
    return runCatching {
        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UPDATE_TAG).get()
        workInfos.firstOrNull()?.state
    }.getOrElse { e ->
        logException(e)
        null
    }
}

/**
 * Return true if any work for UPDATE_TAG is currently enqueued.
 */
fun isWorkerEnqueued(context: Context): Boolean {
    return runCatching {
        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UPDATE_TAG).get()
        workInfos.any { it.state == WorkInfo.State.ENQUEUED }
    }.getOrElse { e ->
        logException(e)
        false
    }
}

/**
 * Cancel the periodic worker and trigger a one-time immediate update.
 */
fun forceUpdate(context: Context) {
    cancelWorker(context)
    startOneTimeWorker(context)
}

/**
 * Return true if any work for UPDATE_TAG is in CANCELLED state.
 */
fun isWorkerCancelled(context: Context): Boolean {
    return runCatching {
        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UPDATE_TAG).get()
        workInfos.any { it.state == WorkInfo.State.CANCELLED }
    }.getOrElse { e ->
        logException(e)
        false
    }
}

/**
 * Reschedule the periodic worker with a new interval.
 */
fun rescheduleWorker(context: Context, newIntervalHours: Long) {
    restartWorker(context, newIntervalHours)
}

/**
 * Return true if any existing work has finished (either SUCCEEDED or FAILED).
 */
fun isWorkerFinished(context: Context): Boolean {
    return runCatching {
        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UPDATE_TAG).get()
        workInfos.any { it.state == WorkInfo.State.SUCCEEDED || it.state == WorkInfo.State.FAILED }
    }.getOrElse { e ->
        logException(e)
        false
    }
}
 
