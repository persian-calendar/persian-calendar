package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context

object ReleaseDebugDifference {
    fun mainApplication(app: Application?) {} // Nothing here
    fun startLynxListenerIfIsDebug(context: Context?) {} // Nothing here
    fun logDebug(tag: String, msg: String) {} // Nothing Here
    val <T> T.debugAssertNotNull: T
        inline get() = this // Nothing here
}