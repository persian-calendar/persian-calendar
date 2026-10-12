package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.CancellationSignal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.DeviceEventInterval
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.resumeToken
import com.byagowi.persiancalendar.utils.deviceCalendarQuerySettings
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.queryCalendarProvider
import com.byagowi.persiancalendar.utils.readDeviceEvents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days

enum class CalendarProviderStatus { Loading, Ready, PermissionDenied, Error }

data class CalendarProviderState<T>(
    val data: T,
    val status: CalendarProviderStatus,
    val error: Throwable? = null,
)

/**
 * A single-result cache owned by this composition, not a process-wide cache of private events.
 * New keys clear old data immediately. Pause/dispose cancels I/O; resume reloads it.
 * Only immutable request inputs enter query; all state publication happens on Main.
 */
@Composable
fun <T> rememberCalendarProviderQuery(
    key: Any?,
    empty: T,
    debounceMillis: Long = 0,
    enabled: Boolean = true,
    query: (CancellationSignal) -> T,
): CalendarProviderState<T> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val generation = resumeToken
    val latestQuery by rememberUpdatedState(query)
    var state by remember(key, generation, enabled, lifecycle, debounceMillis) {
        mutableStateOf(
            CalendarProviderState(
                empty,
                if (enabled) CalendarProviderStatus.Loading else CalendarProviderStatus.Ready,
            ),
        )
    }
    LaunchedEffect(key, generation, enabled, lifecycle, debounceMillis) {
        if (!enabled) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            state = CalendarProviderState(empty, CalendarProviderStatus.Loading)
            runCatching {
                if (debounceMillis > 0) delay(debounceMillis)
                // Read the latest closure on Main; do not read Compose state in the I/O lambda.
                val request = latestQuery
                val result = queryCalendarProvider(request)
                state = CalendarProviderState(result, CalendarProviderStatus.Ready)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                if (error is SecurityException) {
                    state =
                        CalendarProviderState(empty, CalendarProviderStatus.PermissionDenied, error)
                } else {
                    logException(error)
                    state = CalendarProviderState(empty, CalendarProviderStatus.Error, error)
                }
            }
        }
    }
    return state
}

@Composable
fun rememberDeviceCalendarEvents(
    jdn: Jdn,
    days: Int,
    limit: Int = 100,
    refreshToken: Int = 0,
): CalendarProviderState<DeviceCalendarEventsStore> {
    val context = LocalContext.current.applicationContext
    val settings = deviceCalendarQuerySettings()
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED
    val key = listOf(jdn, days, limit, refreshToken, settings, granted)
    val empty = remember { DeviceCalendarEventsStore.empty() }
    val result =
        rememberCalendarProviderQuery(key, empty, enabled = settings.enabled && granted) { signal ->
            DeviceCalendarEventsStore(
                readDeviceEvents(
                    context,
                    DeviceEventInterval.startOfDay(jdn.toCivilDate(), settings.timeZone),
                    days.days,
                    limit,
                    settings = settings,
                    cancellationSignal = signal,
                ),
            )
        }
    return if (settings.enabled && !granted) CalendarProviderState(
        empty,
        CalendarProviderStatus.PermissionDenied,
    ) else result
}

@Composable
fun rememberDeviceCalendarSearch(
    searchTerm: String?,
    today: Jdn,
): CalendarProviderState<List<CalendarEvent.DeviceCalendarEvent>> {
    val context = LocalContext.current.applicationContext
    val settings = deviceCalendarQuerySettings()
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED
    val generation = resumeToken
    val now =
        remember(searchTerm, today, settings, granted, generation) { System.currentTimeMillis() }
    val key = listOf(searchTerm, today, settings, granted, now)
    val result = rememberCalendarProviderQuery(
        key, emptyList(), debounceMillis = 250,
        enabled = !searchTerm.isNullOrBlank() && settings.enabled && granted,
    ) { signal ->
        val start = GregorianCalendar(settings.timeZone).apply {
            timeInMillis = now
            add(GregorianCalendar.YEAR, -1)
        }
        val events = readDeviceEvents(context, start, 730.days, 100, searchTerm, settings, signal)
        val (upcoming, past) = events.partition { it.interval.beginMillis >= now }
        (upcoming.sortedBy { it.interval.beginMillis } +
                past.sortedByDescending { it.interval.beginMillis }).take(100)
    }
    return if (settings.enabled && !granted) CalendarProviderState(
        emptyList(),
        CalendarProviderStatus.PermissionDenied,
    ) else result
}
