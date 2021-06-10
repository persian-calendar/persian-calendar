package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context

object ReleaseDebugDifference {
    fun mainApplication(_: Application?) {} // Nothing here
    fun startLynxListenerIfIsDebug(_: Context?) {} // Nothing here
    fun logDebug(_: String, _: String) {} // Nothing Here
    inline val <T> T.debugAssertNotNull: T inline get() = this // Nothing here
}
