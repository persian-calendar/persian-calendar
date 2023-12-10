package com.byagowi.persiancalendar.utils

import android.util.Log
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.coordinates
import io.github.cosinekitty.astronomy.Observer
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.MidnightMethod
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.GregorianCalendar
import kotlin.math.abs

// .split() turns an empty string into an array with an empty string which is undesirable
// for our use so this filter any non empty string after split, its name rhymes with .filterNotNull
fun String.splitFilterNotEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun Coordinates.calculatePrayTimes(
    calendar: GregorianCalendar = GregorianCalendar(),
    calculationMethod: CalculationMethod = com.byagowi.persiancalendar.global.calculationMethod,
    asrMethod: AsrMethod = com.byagowi.persiancalendar.global.asrMethod,
    highLatitudesMethod: HighLatitudesMethod = com.byagowi.persiancalendar.global.highLatitudesMethod,
    midnightMethod: MidnightMethod = com.byagowi.persiancalendar.global.midnightMethod,
): PrayTimes {
    val year = calendar[GregorianCalendar.YEAR]
    val month = calendar[GregorianCalendar.MONTH] + 1
    val day = calendar[GregorianCalendar.DAY_OF_MONTH]
    val offset = (calendar.timeZone.getOffset(calendar.time.time) / (60 * 60 * 1000.0))
        // This turns GMT+4:30 to GMT+3:30 as Iran has abandoned summer but older devices aren't unaware
        .let { if (it == 4.5 && calendar.timeZone.id == IRAN_TIMEZONE_ID) 3.5 else it }
    return PrayTimes(
        calculationMethod, year, month, day, offset, this, asrMethod, highLatitudesMethod,
        midnightMethod
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
val enableHighLatitudesConfiguration: Boolean
    get() = coordinates.value?.let { abs(it.latitude) > 50 } ?: false

val HighLatitudesMethod.titleStringId
    get(): @StringRes Int = when (this) {
        HighLatitudesMethod.NightMiddle -> R.string.high_latitudes_night_middle
        HighLatitudesMethod.AngleBased -> R.string.high_latitudes_angle_based
        HighLatitudesMethod.OneSeventh -> R.string.high_latitudes_one_seventh
        HighLatitudesMethod.None -> R.string.none
    }

/**
 * Returns the linear interpolation of `amount` between `start` and `stop`.
 *
 * Source: https://github.com/material-components/material-components-android/blob/dfa474fd/lib/java/com/google/android/material/math/MathUtils.java#L36)
 */
fun lerp(start: Float, stop: Float, amount: Float): Float = (1 - amount) * start + amount * stop
