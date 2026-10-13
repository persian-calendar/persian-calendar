package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.utils.logException

// Cross-cutting App-scoped invalidation only; no provider data is stored in this local.
@Suppress("ComposeCompositionLocalUsage")
internal val LocalCalendarProviderRevision = compositionLocalOf { 0 }

/** One App-scoped invalidation source. Merely entering RESUMED never increments the revision. */
@Composable
internal fun rememberCalendarProviderRevision(): Int {
    val context = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val enabled = isShowDeviceCalendarEvents
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED
    var revision by remember { mutableIntStateOf(0) }
    // Re-register after an in-app grant/enable change even when the activity did not pause.
    DisposableEffect(context, lifecycle, enabled, granted) {
        val handler = Handler(Looper.getMainLooper())
        var generation = 0
        var deactivate: (() -> Unit)? = null

        fun stop() {
            deactivate?.invoke()
            deactivate = null
        }

        fun start() {
            if (deactivate != null) return
            val activeGeneration = ++generation
            val publish = Runnable {
                if (generation == activeGeneration &&
                    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                ) ++revision
            }
            fun changed() {
                if (generation != activeGeneration ||
                    !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                ) return
                // Calendar providers can notify several descendant URIs for one edit.
                handler.removeCallbacks(publish)
                handler.postDelayed(publish, 100L)
            }

            val observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) = changed()
            }
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
                        Intent.ACTION_TIMEZONE_CHANGED,
                        -> changed()
                    }
                }
            }
            var observing = false
            var receiving = false
            deactivate = {
                // Invalidate callbacks before unregistering, including already-queued onChange.
                ++generation
                handler.removeCallbacks(publish)
                if (observing) runCatching {
                    context.contentResolver.unregisterContentObserver(observer)
                }.onFailure(logException)
                if (receiving) runCatching {
                    context.unregisterReceiver(receiver)
                }.onFailure(logException)
            }
            if (enabled && granted) runCatching {
                context.contentResolver.registerContentObserver(
                    CalendarContract.CONTENT_URI, true, observer,
                )
                observing = true
            }.onFailure(logException)
            runCatching {
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    IntentFilter().apply {
                        addAction(Intent.ACTION_DATE_CHANGED)
                        addAction(Intent.ACTION_TIME_CHANGED)
                        addAction(Intent.ACTION_TIMEZONE_CHANGED)
                    },
                    ContextCompat.RECEIVER_EXPORTED,
                )
                receiving = true
            }.onFailure(logException)
        }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> start()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> stop()
                else -> Unit
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) start()
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            stop()
        }
    }
    return revision
}
