package com.byagowi.persiancalendar.utils

import android.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine

private val calendarProviderDispatcher = Dispatchers.IO.limitedParallelism(2)

/** Cancels the actual provider operation, including a query blocked before it returns a cursor. */
suspend fun <T> queryCalendarProvider(query: (CancellationSignal) -> T): T =
    suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { signal.cancel() }
        calendarProviderDispatcher.dispatch(
            continuation.context,
            Runnable {
                if (!continuation.isActive) return@Runnable
                val result = runCatching {
                    signal.throwIfCanceled()
                    query(signal).also { signal.throwIfCanceled() }
                }
                // A late provider response cannot revive a canceled caller. The caller resumes on its
                // own dispatcher (Main for UI), after the query's use{} has closed the cursor.
                continuation.resumeWith(result)
            },
        )
    }
