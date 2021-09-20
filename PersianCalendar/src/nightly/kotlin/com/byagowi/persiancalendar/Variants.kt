package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context
import android.util.Log

@Suppress("UNUSED_PARAMETER")
object Variants {
    fun mainApplication(app: Application?) {} // Nothing here
    fun debugLog(message: String) = Log.d(LOG_TAG, message)
    inline val <T> T.debugAssertNotNull: T inline get() = checkNotNull(this)
    inline val enableDevelopmentFeatures get() = true
}
