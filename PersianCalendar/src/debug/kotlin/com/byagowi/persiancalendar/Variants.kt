package com.byagowi.persiancalendar

import android.app.Application
import android.util.Log

object Variants {
    fun mainApplication(app: Application?) {} // Nothing here
    fun debugLog(message: String) = Log.d(LOG_TAG, message)
    inline val <T> T.debugAssertNotNull: T inline get() = checkNotNull(this)
}
