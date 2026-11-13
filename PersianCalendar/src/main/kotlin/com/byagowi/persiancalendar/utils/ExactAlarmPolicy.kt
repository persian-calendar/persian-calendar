package com.byagowi.persiancalendar.utils

internal class ExactAlarmAccessMonitor(initialAccess: Boolean) {
    private var hadAccess = initialAccess

    fun refresh(hasAccess: Boolean, reschedule: () -> Unit) {
        val newlyGranted = hasAccess && !hadAccess
        hadAccess = hasAccess
        if (newlyGranted) reschedule()
    }
}

internal fun scheduleWithExactAlarmFallback(
    canScheduleExact: () -> Boolean,
    scheduleExact: () -> Unit,
    scheduleInexact: () -> Unit,
    onPermissionFailure: (SecurityException) -> Unit,
) {
    // Permission can disappear between the check and the scheduling call.
    runCatching {
        if (canScheduleExact()) {
            scheduleExact()
            return
        }
    }.onFailure { exception ->
        if (exception is SecurityException) onPermissionFailure(exception)
        else logException(exception)
    }
    runCatching {
        scheduleInexact()
    }.onFailure { exception ->
        if (exception is SecurityException) {
            // Keep the WorkManager backup already enqueued by the caller, even on an OEM failure.
            onPermissionFailure(exception)
        } else logException(exception)
    }
}
