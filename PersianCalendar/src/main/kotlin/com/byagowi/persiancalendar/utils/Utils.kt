package com.byagowi.persiancalendar.utils

import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import io.github.cosinekitty.astronomy.Observer
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.*
import kotlin.math.abs

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun Coordinates.calculatePrayTimes(
    calendar: GregorianCalendar = GregorianCalendar(),
    calculationMethod: CalculationMethod = com.byagowi.persiancalendar.global.calculationMethod,
    asrMethod: AsrMethod = com.byagowi.persiancalendar.global.asrMethod,
    highLatitudesMethod: HighLatitudesMethod = com.byagowi.persiancalendar.global.highLatitudesMethod
): PrayTimes {
    val year = calendar[GregorianCalendar.YEAR]
    val month = calendar[GregorianCalendar.MONTH] + 1
    val day = calendar[GregorianCalendar.DAY_OF_MONTH]
    val offset = calendar.timeZone.getOffset(calendar.time.time) / (60 * 60 * 1000.0)
    return PrayTimes(
        calculationMethod, year, month, day, offset, this, asrMethod, highLatitudesMethod
    )
}

val Coordinates.isSouthernHemisphere get() = latitude < .0

fun Coordinates.toObserver() = Observer(this.latitude, this.longitude, this.elevation)

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

fun Bundle.putJdn(key: String, jdn: Jdn) = putLong(key, jdn.value)
