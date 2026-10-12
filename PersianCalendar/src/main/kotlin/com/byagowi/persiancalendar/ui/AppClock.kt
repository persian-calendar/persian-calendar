package com.byagowi.persiancalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

/** Updates display time while resumed, without invalidating calendar-provider requests. */
@Composable
internal fun rememberAppClock(clock: () -> Long = System::currentTimeMillis): State<Long> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val latestClock by rememberUpdatedState(clock)
    return produceState(initialValue = clock(), key1 = lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                value = latestClock()
                delay(30.seconds)
            }
        }
    }
}
