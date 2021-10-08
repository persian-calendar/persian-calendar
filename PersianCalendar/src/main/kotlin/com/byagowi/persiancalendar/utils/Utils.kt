package com.byagowi.persiancalendar.utils

import android.util.Log
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.entities.Jdn
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.*

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun Coordinates.calculatePrayTimes(calendar: GregorianCalendar = GregorianCalendar()) =
    PrayTimes(calculationMethod, calendar, this, asrMethod)

fun Coordinates?.calculateMoonPhase(jdn: Jdn) = runCatching {
    this ?: return@runCatching 1.0
    SunMoonPosition(jdn.value.toDouble(), latitude, longitude, elevation, 0.0).moonPhase
}.onFailure(logException).getOrNull() ?: 1.0

val logException = fun(e: Throwable) { Log.e(LOG_TAG, "Handled Exception", e) }
