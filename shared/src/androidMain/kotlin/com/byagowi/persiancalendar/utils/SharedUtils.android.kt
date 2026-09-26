package com.byagowi.persiancalendar.utils

import android.util.Log
import com.byagowi.persiancalendar.LOG_TAG

actual val logException: (Throwable) -> Unit = fun(e: Throwable) { Log.e(LOG_TAG, "Handled Exception", e) }

actual fun debugLog(vararg message: Any?) {
    if (isDebugBuild) Log.d(LOG_TAG, message.joinToString(", "))
}

actual var isDebugBuild: Boolean = false
