package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.entities.Jdn
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*

fun isNightModeEnabled(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun Coordinate.calculatePrayTimes(date: Date = Date()): PrayTimes =
    PrayTimesCalculator.calculate(calculationMethod, date, this, asrJuristic)

val CalculationMethod.isShia: Boolean
    get() = when (this) {
        CalculationMethod.Tehran, CalculationMethod.Jafari -> true
        else -> false
    }

fun Coordinate?.calculateMoonPhase(jdn: Jdn) = runCatching {
    this ?: return@runCatching 1.0
    SunMoonPosition(jdn.value.toDouble(), latitude, longitude, elevation, 0.0).moonPhase
}.onFailure(logException).getOrNull() ?: 1.0

val logException = fun(e: Throwable) { Log.e(LOG_TAG, "Handled Exception", e) }
