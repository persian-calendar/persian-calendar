package com.byagowi.persiancalendar.variants

import android.util.Log
import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.LOG_TAG

@OptIn(ExperimentalComposeRuntimeApi::class)
fun mainApplicationOnCreateHook() {
    Composer.setDiagnosticStackTraceEnabled(BuildConfig.DEBUG)
}

fun debugLog(vararg message: Any?) {
    Log.d(LOG_TAG, message.joinToString(", "))
}

inline val <T> T.debugAssertNotNull: T inline get() = checkNotNull(this)
