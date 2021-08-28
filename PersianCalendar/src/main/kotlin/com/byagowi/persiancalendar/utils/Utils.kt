package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log

fun isNightModeEnabled(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

val logException = fun(e: Throwable) { Log.e("Persian Calendar", e.message, e) }
