package com.byagowi.persiancalendar.utils

import android.util.Log
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.R
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

// Thee same order as http://praytimes.org/code/v2/js/examples/monthly.htm
val CalculationMethod.titleStringId
    get(): @StringRes Int = when (this) {
        CalculationMethod.MWL -> R.string.method_mwl
        CalculationMethod.ISNA -> R.string.method_isna
        CalculationMethod.Egypt -> R.string.method_egypt
        CalculationMethod.Makkah -> R.string.method_makkah
        CalculationMethod.Karachi -> R.string.method_karachi
        CalculationMethod.Jafari -> R.string.method_jafari
        CalculationMethod.Tehran -> R.string.method_tehran
    }
