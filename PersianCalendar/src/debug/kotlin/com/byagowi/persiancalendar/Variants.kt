package com.byagowi.persiancalendar

import android.util.Log

object Variants {
    fun debugLog(message: String) = Log.d(LOG_TAG, message)
    inline val <T> T.debugAssertNotNull: T inline get() = checkNotNull(this)
}
