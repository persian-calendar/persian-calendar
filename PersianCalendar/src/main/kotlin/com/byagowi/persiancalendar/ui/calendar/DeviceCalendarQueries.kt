package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.CancellationSignal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.byagowi.persiancalendar.utils.DeviceCalendarQuerySettings
import com.byagowi.persiancalendar.utils.deviceCalendarQuerySettings
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.queryCalendarProvider
import com.byagowi.persiancalendar.utils.readDeviceEvents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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
    refreshToken: Int,
    limit: Int = 100,
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

private const val DEVICE_EVENTS_CACHE_DAY_LIMIT = 120
private const val DEVICE_EVENTS_CACHE_CAPACITY = 90

private data class DeviceEventsEnvironment(
    val settings: DeviceCalendarQuerySettings,
    val permissionGranted: Boolean,
    val revision: Int,
    val refreshToken: Int,
    val generation: Int,
) {
    val canQuery get() = settings.enabled && permissionGranted
}

private data class DeviceEventsRequest(
    val startJdn: Jdn,
    val days: Int,
    val limit: Int,
) {
    val isDay get() = days == 1 && limit == DEVICE_EVENTS_CACHE_DAY_LIMIT

    fun covers(jdn: Jdn) = (jdn - startJdn) in 0 until days
}

private fun deviceEventsOfDayRequest(jdn: Jdn) = DeviceEventsRequest(jdn, 1, DEVICE_EVENTS_CACHE_DAY_LIMIT)

private class CachedDeviceEvents(
    val state: CalendarProviderState<DeviceCalendarEventsStore>,
    val generation: Int,
)

/** A preloadable LRU cache of device calendar events, keyed by the requested range. */
class DeviceCalendarEventsCache internal constructor(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val emptyDeviceEvents = DeviceCalendarEventsStore.empty()
    private val entries = mutableStateMapOf<DeviceEventsRequest, CachedDeviceEvents>()
    private val accessOrder = LinkedHashMap<DeviceEventsRequest, Unit>(16, .75f, true)
    private val requests = mutableMapOf<DeviceEventsRequest, Job>()
    private var environment by mutableStateOf<DeviceEventsEnvironment?>(null)
    private var isResumed = false
    private var preloadStart: Jdn? = null
    private var preloadDays = 0
    private var activePreloadRequest: DeviceEventsRequest? = null

    /**
     * Requests [days] days of device calendar events starting at [startJdnRange]. The whole range
     * is loaded in a single provider query and split into day entries, so a row that scrolls in
     * has its final height right away.
     */
    @Suppress("ComposableNaming", "ComposeNamingUppercase")
    @Composable
    fun preloadDeviceEvents(startJdnRange: Jdn, days: Int) {
        if (days <= 0) return
        LaunchedEffect(startJdnRange, days) {
            preloadStart = startJdnRange
            preloadDays = days
            requestPreloadedDeviceEvents(force = false)
        }
        // The range is only protected from eviction while its screen is present.
        DisposableEffect(Unit) {
            onDispose {
                preloadStart = null
                preloadDays = 0
            }
        }
    }

    /** Observes a day's cached device events and requests it when it is missing or outdated. */
    @Composable
    fun getDeviceEventsOfDay(jdn: Jdn): CalendarProviderState<DeviceCalendarEventsStore> =
        getDeviceEvents(deviceEventsOfDayRequest(jdn))

    /** Observes a range's cached device events and requests it when it is missing or outdated. */
    @Composable
    fun getDeviceEventsOfDays(
        startJdn: Jdn,
        days: Int,
        limit: Int = 100,
    ): CalendarProviderState<DeviceCalendarEventsStore> =
        getDeviceEvents(DeviceEventsRequest(startJdn, days, limit))

    @Composable
    private fun getDeviceEvents(request: DeviceEventsRequest): CalendarProviderState<DeviceCalendarEventsStore> {
        val currentEnvironment = environment
        val cached by remember(request) { derivedStateOf { entries[request] } }
        LaunchedEffect(request, currentEnvironment) { requestDeviceEvents(request, force = false) }
        return stateOf(currentEnvironment, cached)
    }

    internal fun update(
        settings: DeviceCalendarQuerySettings,
        permissionGranted: Boolean,
        revision: Int,
        refreshToken: Int,
    ) {
        val previous = environment
        if (previous != null &&
            previous.settings == settings &&
            previous.permissionGranted == permissionGranted &&
            previous.revision == revision &&
            previous.refreshToken == refreshToken
        ) return
        environment = DeviceEventsEnvironment(
            settings = settings,
            permissionGranted = permissionGranted,
            revision = revision,
            refreshToken = refreshToken,
            generation = (previous?.generation ?: 0) + 1,
        )
        cancelRequests()
        requestPreloadedDeviceEvents(force = false)
    }

    /**
     * Holds requests while the app is not resumed and reloads on resume, as provider change
     * notifications do not run while the app is paused.
     */
    internal suspend fun runWhileResumed(): Unit = suspendCancellableCoroutine { continuation ->
        isResumed = true
        // What was loaded before the pause may have changed while observers were off.
        if (entries.isNotEmpty()) invalidate()
        continuation.invokeOnCancellation {
            isResumed = false
            cancelRequests()
        }
        requestPreloadedDeviceEvents(force = false)
    }

    private fun invalidate() {
        val current = environment ?: return
        environment = current.copy(generation = current.generation + 1)
        cancelRequests()
    }

    private fun stateOf(
        environment: DeviceEventsEnvironment?,
        cached: CachedDeviceEvents?,
    ): CalendarProviderState<DeviceCalendarEventsStore> = when {
        environment == null -> CalendarProviderState(emptyDeviceEvents, CalendarProviderStatus.Loading)
        !environment.settings.enabled -> CalendarProviderState(emptyDeviceEvents, CalendarProviderStatus.Ready)
        !environment.permissionGranted ->
            CalendarProviderState(emptyDeviceEvents, CalendarProviderStatus.PermissionDenied)

        cached == null -> CalendarProviderState(emptyDeviceEvents, CalendarProviderStatus.Loading)
        cached.generation == environment.generation -> cached.state
        // Keep the previous result while refreshing so lazy-list rows do not collapse and cancel
        // a backward fling.
        else -> CalendarProviderState(
            cached.state.data,
            CalendarProviderStatus.Loading,
            hasResult = cached.state.hasResult,
        )
    }

    private fun requestPreloadedDeviceEvents(force: Boolean) {
        val start = preloadStart ?: return
        val days = preloadDays
        if (days <= 0) return
        val environment = environment ?: return
        if (!environment.canQuery || !isResumed) return
        val missing = (0 until days).map { start + it }.filter { day ->
            force || entries[deviceEventsOfDayRequest(day)]?.generation != environment.generation
        }
        if (missing.isEmpty()) return
        // Query the whole missing span at once instead of one provider query per day.
        val first = missing.minBy { it.value }
        val last = missing.maxBy { it.value }
        val count = last - first + 1
        val request = DeviceEventsRequest(first, count, DEVICE_EVENTS_CACHE_DAY_LIMIT * count)
        if (requests.containsKey(request)) return
        activePreloadRequest = request
        val job = scope.launch {
            val state = loadDeviceEvents(request, environment)
            if (state.status == CalendarProviderStatus.Ready) {
                repeat(count) { offset ->
                    val jdn = first + offset
                    publish(
                        deviceEventsOfDayRequest(jdn),
                        CalendarProviderState(
                            DeviceCalendarEventsStore(state.data.getEvents(jdn.toCivilDate())),
                            CalendarProviderStatus.Ready,
                        ),
                        environment.generation,
                    )
                }
            } else {
                repeat(count) { offset ->
                    publish(deviceEventsOfDayRequest(first + offset), state, environment.generation)
                }
            }
        }
        requests[request] = job
        job.invokeOnCompletion {
            if (requests[request] === job) requests.remove(request)
            if (activePreloadRequest === request) activePreloadRequest = null
        }
    }

    private fun requestDeviceEvents(request: DeviceEventsRequest, force: Boolean) {
        // Preloading and reading a range both count as using it, which keeps it out of eviction.
        if (entries.containsKey(request)) accessOrder[request] = Unit
        val environment = environment ?: return
        // The preload loader publishes these days in bulk, do not query them one by one as well.
        if (request.isDay && activePreloadRequest?.covers(request.startJdn) == true) return
        if (!environment.canQuery || !isResumed) return
        if (requests.containsKey(request)) return
        if (!force && entries[request]?.generation == environment.generation) return
        val job = scope.launch {
            publish(request, loadDeviceEvents(request, environment), environment.generation)
        }
        requests[request] = job
        job.invokeOnCompletion { if (requests[request] === job) requests.remove(request) }
    }

    private suspend fun loadDeviceEvents(
        request: DeviceEventsRequest,
        environment: DeviceEventsEnvironment,
    ): CalendarProviderState<DeviceCalendarEventsStore> = runCatching {
        queryCalendarProvider { signal ->
            DeviceCalendarEventsStore(
                readDeviceEvents(
                    context,
                    DeviceEventInterval.startOfDay(
                        request.startJdn.toCivilDate(),
                        environment.settings.timeZone,
                    ),
                    request.days.days,
                    request.limit,
                    settings = environment.settings,
                    cancellationSignal = signal,
                ),
            )
        }
    }.fold(
        onSuccess = { CalendarProviderState(it, CalendarProviderStatus.Ready) },
        onFailure = { error ->
            if (error is CancellationException) throw error
            if (error is SecurityException) {
                CalendarProviderState(emptyDeviceEvents, CalendarProviderStatus.PermissionDenied, error)
            } else {
                logException(error)
                CalendarProviderState(emptyDeviceEvents, CalendarProviderStatus.Error, error)
            }
        },
    )

    private fun publish(
        request: DeviceEventsRequest,
        state: CalendarProviderState<DeviceCalendarEventsStore>,
        generation: Int,
    ) {
        // A result from before an invalidation must not overwrite a newer request's state.
        if (environment?.generation != generation) return
        entries[request] = CachedDeviceEvents(state, generation)
        accessOrder[request] = Unit
        trim()
    }

    private fun trim() {
        if (entries.size <= DEVICE_EVENTS_CACHE_CAPACITY) return
        val start = preloadStart
        val iterator = accessOrder.entries.iterator()
        while (entries.size > DEVICE_EVENTS_CACHE_CAPACITY && iterator.hasNext()) {
            val request = iterator.next().key
            if (requests.containsKey(request)) continue
            if (request.isDay && start != null && preloadDays > 0 &&
                (request.startJdn - start) in 0 until preloadDays
            ) continue
            iterator.remove()
            entries.remove(request)
        }
    }

    private fun cancelRequests() {
        requests.values.forEach { it.cancel() }
        requests.clear()
        activePreloadRequest = null
    }
}

/**
 * Creates a [DeviceCalendarEventsCache] whose cached ranges are invalidated by [refreshToken] or
 * a provider change; the most recently preloaded range is reloaded after an invalidation.
 */
@Composable
fun rememberDeviceCalendarEventsCache(refreshToken: Int): DeviceCalendarEventsCache {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val providerRevision = LocalCalendarProviderRevision.current
    val cache = remember(context, scope) { DeviceCalendarEventsCache(context, scope) }
    // Settings are snapshot-backed, so sample them here and never inside the query.
    val settings = deviceCalendarQuerySettings()
    val permissionGranted = context.hasCalendarPermission()
    LaunchedEffect(cache, settings, permissionGranted, providerRevision, refreshToken) {
        cache.update(settings, permissionGranted, providerRevision, refreshToken)
    }
    LaunchedEffect(cache, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { cache.runWhileResumed() }
    }
    return cache
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
