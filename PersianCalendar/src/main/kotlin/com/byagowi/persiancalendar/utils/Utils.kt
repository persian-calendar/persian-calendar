package com.byagowi.persiancalendar.utils

import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.asrMethod
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.highLatitudesMethod
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.*
import kotlin.math.abs

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun Coordinates.calculatePrayTimes(calendar: GregorianCalendar = GregorianCalendar()) =
    PrayTimes(calculationMethod, calendar, this, asrMethod, highLatitudesMethod)

fun GregorianCalendar.calculateSunMoonPosition(coordinates: Coordinates?) =
    SunMoonPosition(this, coordinates, 0.0)

val Coordinates.isSouthernHemisphere get() = latitude < .0

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

// Midnight sun occurs at latitudes from 65°44' to 90° north or south as
// https://en.wikipedia.org/wiki/Midnight_sun
val enableHighLatitudesConfiguration get() = coordinates?.let { abs(it.latitude) > 50 } ?: false

val HighLatitudesMethod.titleStringId
    get(): @StringRes Int = when (this) {
        HighLatitudesMethod.NightMiddle -> R.string.high_latitudes_night_middle
        HighLatitudesMethod.AngleBased -> R.string.high_latitudes_angle_based
        HighLatitudesMethod.OneSeventh -> R.string.high_latitudes_one_seventh
        HighLatitudesMethod.None -> R.string.none
    }

fun Bundle.putJdn(key: String, jdn: Jdn?) {
    if (jdn == null) remove(jdn) else putLong(key, jdn.value)
}

fun Bundle.getJdnOrNull(key: String): Jdn? =
    getLong(key, -1).takeIf { it != -1L }?.let { Jdn(it) }
