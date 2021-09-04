package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.byagowi.persiancalendar.LOG_TAG
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*

fun isNightModeEnabled(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun Coordinate.calculatePrayTimes(date: Date = Date()): PrayTimes =
    PrayTimesCalculator.calculate(calculationMethod, date, this)

val logException = fun(e: Throwable) { Log.e(LOG_TAG, "Handled Exception", e) }
