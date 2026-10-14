package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.utils.ExactAlarmAccessMonitor
import com.byagowi.persiancalendar.utils.canScheduleExactAlarms
import com.byagowi.persiancalendar.utils.rescheduleAlarmsWithExactAccess

@Composable
internal fun rememberExactAlarmAccess(): Boolean {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var hasAccess by remember(context) { mutableStateOf(canScheduleExactAlarms(context)) }
    val monitor = remember(context) { ExactAlarmAccessMonitor(hasAccess) }
    DisposableEffect(context, lifecycle, monitor) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccess = canScheduleExactAlarms(context)
                monitor.refresh(hasAccess) { rescheduleAlarmsWithExactAccess(context) }
            }
        }
        // addObserver also dispatches ON_RESUME when this screen is already resumed.
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return hasAccess
}
