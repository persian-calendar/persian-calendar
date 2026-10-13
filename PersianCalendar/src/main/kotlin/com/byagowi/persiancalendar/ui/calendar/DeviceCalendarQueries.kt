package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.CancellationSignal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.DeviceEventInterval
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.deviceCalendarQuerySettings
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.queryCalendarProvider
import com.byagowi.persiancalendar.utils.readDeviceEvents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

enum class CalendarProviderStatus { Loading, Ready, PermissionDenied, Error }

// Use publication identity: CalendarEvent.equals deliberately ignores device time/description.
// A fresh successful result must invalidate consumers even when those event lists compare equal.
class CalendarProviderState<T>(
    val data: T,
    val status: CalendarProviderStatus,
    val error: Throwable? = null,
    val hasResult: Boolean = status == CalendarProviderStatus.Ready,
)

private class CalendarProviderRequest<T>(
    val key: Any?,
    val refreshKey: Any?,
    val enabled: Boolean,
    val permissionGranted: Boolean,
    val debounceMillis: Long,
    val prepareQuery: () -> (CancellationSignal) -> T,
) {
    val contentKey get() = listOf(key, enabled, permissionGranted)

    // Query lambdas and the wall clock are deliberately not request identities.
    val identity get() = listOf(contentKey, refreshKey, debounceMillis)
}

private data class CalendarProviderResult<T>(
    val contentKey: Any?,
    val state: CalendarProviderState<T>,
)

/** Only the RESUMED collector owns resume reloads; recomposition never starts another owner. */
@Composable
fun <T> rememberCalendarProviderQuery(
    key: Any?,
    empty: T,
    debounceMillis: Long = 0,
    enabled: Boolean = true,
    refreshKey: Any? = Unit,
    permissionGranted: () -> Boolean = { true },
    query: (CancellationSignal) -> T,
): CalendarProviderState<T> = rememberCalendarProviderRequest(empty) {
    CalendarProviderRequest(
        key, refreshKey, enabled, permissionGranted(), debounceMillis,
        prepareQuery = { query },
    )
}

@Composable
private fun <T> rememberCalendarProviderRequest(
    empty: T,
    requestFactory: () -> CalendarProviderRequest<T>,
): CalendarProviderState<T> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val providerRevision = LocalCalendarProviderRevision.current
    val latestRequest by rememberUpdatedState(requestFactory)
    val latestRevision by rememberUpdatedState(providerRevision)
    val latestEmpty by rememberUpdatedState(empty)
    var stored by remember(lifecycle) { mutableStateOf<CalendarProviderResult<T>?>(null) }
    val currentRequest = requestFactory()

    fun emptyState(request: CalendarProviderRequest<T>) = CalendarProviderState(
        empty,
        when {
            !request.enabled -> CalendarProviderStatus.Ready
            !request.permissionGranted -> CalendarProviderStatus.PermissionDenied
            else -> CalendarProviderStatus.Loading
        },
    )

    // Clear private data for a different request/disabled permission even while paused, when the
    // lifecycle collector is not running. A same-request refresh preserves its last result.
    DisposableEffect(currentRequest.contentKey, lifecycle) {
        if (stored?.contentKey != currentRequest.contentKey) {
            stored = CalendarProviderResult(currentRequest.contentKey, emptyState(currentRequest))
        }
        onDispose { }
    }
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            snapshotFlow { latestRequest() to latestRevision }
                .distinctUntilChangedBy { (request, revision) -> request.identity to revision }
                .collectLatest { (request, revision) ->
                    fun publish(state: CalendarProviderState<T>) {
                        // A changed input can precede collectLatest's cancellation on Main.
                        val current = latestRequest()
                        if (!current.enabled || !current.permissionGranted) {
                            stored = CalendarProviderResult(
                                current.contentKey,
                                CalendarProviderState(
                                    latestEmpty,
                                    if (current.enabled) CalendarProviderStatus.PermissionDenied
                                    else CalendarProviderStatus.Ready,
                                ),
                            )
                        } else if (request.identity == current.identity && revision == latestRevision) {
                            stored = CalendarProviderResult(request.contentKey, state)
                        }
                    }
                    if (!request.enabled || !request.permissionGranted) {
                        publish(
                            CalendarProviderState(
                                latestEmpty,
                                if (request.enabled) {
                                    CalendarProviderStatus.PermissionDenied
                                } else CalendarProviderStatus.Ready,
                            ),
                        )
                        return@collectLatest
                    }
                    val previous = stored?.takeIf { it.contentKey == request.contentKey }?.state
                    publish(
                        CalendarProviderState(
                            previous?.data ?: latestEmpty, CalendarProviderStatus.Loading,
                            hasResult = previous?.hasResult == true,
                        ),
                    )
                    runCatching {
                        if (request.debounceMillis > 0) delay(request.debounceMillis.milliseconds)
                        // Settings and the search reference time are sampled on Main. Only the
                        // immutable prepared query crosses the bounded I/O boundary.
                        val query = request.prepareQuery()
                        val result = queryCalendarProvider(query)
                        currentCoroutineContext().ensureActive()
                        publish(CalendarProviderState(result, CalendarProviderStatus.Ready))
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        if (error is SecurityException) {
                            publish(
                                CalendarProviderState(
                                    latestEmpty,
                                    CalendarProviderStatus.PermissionDenied,
                                    error,
                                ),
                            )
                        } else {
                            logException(error)
                            publish(
                                CalendarProviderState(
                                    latestEmpty,
                                    CalendarProviderStatus.Error,
                                    error,
                                ),
                            )
                        }
                    }
                }
        }
    }
    return stored?.takeIf { it.contentKey == currentRequest.contentKey }?.state
        ?: emptyState(currentRequest)
}

private fun Context.hasCalendarPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

@Composable
fun rememberDeviceCalendarEvents(
    jdn: Jdn,
    days: Int,
    limit: Int = 100,
    refreshToken: Int = 0,
): CalendarProviderState<DeviceCalendarEventsStore> {
    val context = LocalContext.current.applicationContext
    val empty = remember { DeviceCalendarEventsStore.empty() }
    return rememberCalendarProviderRequest(empty) {
        // Read snapshot-backed settings here, not in an old composition's captured closure.
        val settings = deviceCalendarQuerySettings()
        CalendarProviderRequest(
            key = listOf(jdn, days, limit, settings),
            refreshKey = refreshToken,
            enabled = settings.enabled,
            permissionGranted = context.hasCalendarPermission(),
            debounceMillis = 0,
            prepareQuery = {
                { signal ->
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
            },
        )
    }
}

@Composable
fun rememberDeviceCalendarSearch(
    searchTerm: String?,
    today: Jdn,
): CalendarProviderState<List<CalendarEvent.DeviceCalendarEvent>> {
    val context = LocalContext.current.applicationContext
    return rememberCalendarProviderRequest(emptyList()) {
        val settings = deviceCalendarQuerySettings()
        CalendarProviderRequest(
            key = listOf(searchTerm, today, settings),
            refreshKey = Unit,
            enabled = !searchTerm.isNullOrBlank() && settings.enabled,
            permissionGranted = context.hasCalendarPermission(),
            debounceMillis = 250,
            prepareQuery = {
                // A new instant for each real query, never a key which causes another query.
                val now = System.currentTimeMillis()
                val start = GregorianCalendar(settings.timeZone).apply {
                    timeInMillis = now
                    add(GregorianCalendar.YEAR, -1)
                }
                val query: (CancellationSignal) -> List<CalendarEvent.DeviceCalendarEvent> =
                    { signal ->
                        val events = readDeviceEvents(
                            context,
                            start,
                            730.days,
                            100,
                            searchTerm,
                            settings,
                            signal,
                        )
                        val (upcoming, past) = events.partition { it.interval.beginMillis >= now }
                        (upcoming.sortedBy { it.interval.beginMillis } +
                                past.sortedByDescending { it.interval.beginMillis }).take(100)
                    }
                query
            },
        )
    }
}
