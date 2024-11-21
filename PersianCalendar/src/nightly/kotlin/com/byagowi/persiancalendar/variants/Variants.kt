package com.byagowi.persiancalendar.variants

import android.util.Log
import com.byagowi.persiancalendar.LOG_TAG

fun debugLog(vararg message: Any?) {
    Log.d(LOG_TAG, message.toString())
}

inline val <T> T.debugAssertNotNull: T inline get() = checkNotNull(this)
