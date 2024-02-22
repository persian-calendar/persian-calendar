package com.byagowi.persiancalendar.variants

import android.util.Log
import com.byagowi.persiancalendar.LOG_TAG

fun debugLog(message: String) {
    Log.d(LOG_TAG, message)
}

inline val <T> T.debugAssertNotNull: T inline get() = checkNotNull(this)
