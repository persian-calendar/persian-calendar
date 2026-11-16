package com.byagowi.persiancalendar.ui.calendar

/** Keep the reduced budget across queries/invalidations until a healthy foreground session. */
internal class DeviceEventsCacheMemoryPolicy {
    var preloadingAllowed = true
        private set

    val capacity get() = if (preloadingAllowed) 90 else 12

    fun onMemoryPressure() {
        preloadingAllowed = false
    }

    fun onResume(lowMemory: Boolean) {
        preloadingAllowed = !lowMemory
    }
}
