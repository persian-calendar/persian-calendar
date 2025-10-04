package com.byagowi.persiancalendar.utils

import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.asrMethod
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.highLatitudesMethod
import com.byagowi.persiancalendar.global.midnightMethod
import io.github.cosinekitty.astronomy.Observer
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.MidnightMethod
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.*

/**
 * Utilities around prayer time calculations, coordinates and small helpers used by UI.
 * This file fixes several edge-cases and adds nicer formatting helpers for display.
 */

// .split() turns an empty string into an array with an empty string which is undesirable
// for our use so this filters any non empty string after split, its name rhymes with .filterNotNull
fun String.splitFilterNotEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

/** Calculate prayer times using global settings by default. */
fun Coordinates.calculatePrayTimes(calendar: GregorianCalendar = GregorianCalendar()): PrayTimes =
    calculatePrayTimes(
        calendar = calendar,
        calculationMethod = calculationMethod.value,
        asrMethod = asrMethod.value,
        highLatitudesMethod = highLatitudesMethod,
        midnightMethod = midnightMethod,
    )

@VisibleForTesting
fun Coordinates.calculatePrayTimes(
    calendar: GregorianCalendar,
    calculationMethod: CalculationMethod,
    asrMethod: AsrMethod,
    highLatitudesMethod: HighLatitudesMethod,
    midnightMethod: MidnightMethod,
): PrayTimes {
    val year = calendar[GregorianCalendar.YEAR]
    val month = calendar[GregorianCalendar.MONTH] + 1
    val day = calendar[GregorianCalendar.DAY_OF_MONTH]
    val offset = (calendar.timeZone.getOffset(calendar.time.time) / (60 * 60 * 1000.0))
        // This turns GMT+4:30 to GMT+3:30 as Iran has abandoned summer but older devices aren't unaware
        .let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                it == 4.5 && calendar.timeZone.id == IRAN_TIMEZONE_ID
            ) 3.5 else it
        }
    return PrayTimes(
        calculationMethod, year, month, day, offset, this, asrMethod, highLatitudesMethod,
        midnightMethod
    )
}

/** Southern hemisphere helper. */
val Coordinates.isSouthernHemisphere get() = latitude < 0.0

fun Coordinates.toObserver() = Observer(this.latitude, this.longitude, this.elevation)

val logException = fun(e: Throwable) { Log.e(LOG_TAG, "Handled Exception", e) }

// The same order as http://praytimes.org/code/v2/js/examples/monthly.htm
val CalculationMethod.titleStringId
    @StringRes
    get(): Int = when (this) {
        CalculationMethod.MWL -> R.string.method_mwl
        CalculationMethod.ISNA -> R.string.method_isna
        CalculationMethod.Egypt -> R.string.method_egypt
        CalculationMethod.Makkah -> R.string.method_makkah
        CalculationMethod.Karachi -> R.string.method_karachi
        CalculationMethod.Jafari -> R.string.method_jafari
        CalculationMethod.Tehran -> R.string.method_tehran
        else -> R.string.empty
    }

// High-latitude detection
val Coordinates.isHighLatitude: Boolean get() = abs(latitude) > 48

val HighLatitudesMethod.titleStringId
    @StringRes
    get(): Int = when (this) {
        HighLatitudesMethod.NightMiddle -> R.string.high_latitudes_night_middle
        HighLatitudesMethod.AngleBased -> R.string.high_latitudes_angle_based
        HighLatitudesMethod.OneSeventh -> R.string.high_latitudes_one_seventh
        HighLatitudesMethod.None -> R.string.none
        else -> R.string.none
    }

inline val <T> T.debugAssertNotNull: T
    inline get() = if (BuildConfig.DEVELOPMENT) checkNotNull(this) else this

fun debugLog(vararg message: Any?) {
    if (BuildConfig.DEVELOPMENT) Log.d(LOG_TAG, message.joinToString(", "))
}

// ------------------ Additional Utility Functions ------------------

/** Convert decimal hours (e.g. 13.5) to a localized HH:MM string. */
fun hoursToHourMinuteString(hours: Double, locale: Locale = Locale.getDefault()): String {
    val totalMinutes = (hours * 60.0).roundToInt().coerceIn(0, 24 * 60 - 1)
    val hh = totalMinutes / 60
    val mm = totalMinutes % 60
    return String.format(locale, "%02d:%02d", hh, mm)
}

/** Nicely formatted single-line prayer summary suitable for small UI elements. */
fun PrayTimes.toFormattedString(): String =
    listOf(
        "Fajr" to fajr,
        "Dhuhr" to dhuhr,
        "Asr" to asr,
        "Maghrib" to maghrib,
        "Isha" to isha
    ).joinToString(" • ") { (name, time) -> "$name ${hoursToHourMinuteString(time)}" }

/** Full detailed map with HH:mm strings for flexible UI rendering. */
fun PrayTimes.toMapFormatted(locale: Locale = Locale.getDefault()): Map<String, String> = mapOf(
    "Fajr" to hoursToHourMinuteString(fajr, locale),
    "Sunrise" to hoursToHourMinuteString(sunrise, locale),
    "Dhuhr" to hoursToHourMinuteString(dhuhr, locale),
    "Asr" to hoursToHourMinuteString(asr, locale),
    "Maghrib" to hoursToHourMinuteString(maghrib, locale),
    "Isha" to hoursToHourMinuteString(isha, locale),
)

/** Raw numeric map (useful for charts/animations). */
fun PrayTimes.toMap(): Map<String, Double> = mapOf(
    "Fajr" to fajr,
    "Sunrise" to sunrise,
    "Dhuhr" to dhuhr,
    "Asr" to asr,
    "Maghrib" to maghrib,
    "Isha" to isha
)

/** Returns true if coordinates are within [thresholdKm] kilometers of Mecca. */
fun Coordinates.isNearMecca(thresholdKm: Double = 50.0): Boolean {
    val meccaLat = 21.3891
    val meccaLon = 39.8579
    val distance = haversineDistance(latitude, longitude, meccaLat, meccaLon)
    return distance <= thresholdKm
}

/** Calculates distance between two lat/long points in kilometers using the haversine formula. */
fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Earth radius in km
    val dLat = toRadians(lat2 - lat1)
    val dLon = toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(toRadians(lat1)) * cos(toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

/** Returns approximate Qibla direction (in degrees from North). */
fun Coordinates.qiblaDirection(): Double {
    val meccaLat = toRadians(21.3891)
    val meccaLon = toRadians(39.8579)
    val latRad = toRadians(latitude)
    val lonRad = toRadians(longitude)
    val dLon = meccaLon - lonRad

    val y = sin(dLon)
    val x = cos(latRad) * tan(meccaLat) - sin(latRad) * cos(dLon)
    var bearing = toDegrees(atan2(y, x))
    if (bearing < 0) bearing += 360.0
    return bearing
}

/** Return whether the current time (hours/minutes) is between [startHours] and [endHours].
 *  Handles ranges that cross midnight. */
fun isWithinHours(currentHour: Int, currentMinute: Int, startHours: Double, endHours: Double): Boolean {
    val current = currentHour * 60 + currentMinute
    val start = (startHours * 60).roundToInt()
    val end = (endHours * 60).roundToInt()
    return if (start <= end) current in start..end else (current >= start || current <= end)
}

/** Returns whether the provided current time falls into the Dhuhr-Asr interval. */
fun PrayTimes.isCurrentPrayerTime(currentHour: Int, currentMinute: Int): Boolean {
    return isWithinHours(currentHour, currentMinute, dhuhr, asr)
}

/** Readable representation of coordinates. */
fun Coordinates.prettyString(): String =
    String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f, Elev: %.1f m", latitude, longitude, elevation)

/** Midpoint between two coordinates (returns averaged elevation if available). */
fun Coordinates.midpointTo(other: Coordinates): Coordinates {
    val lat1 = toRadians(latitude)
    val lon1 = toRadians(longitude)
    val lat2 = toRadians(other.latitude)
    val dLon = toRadians(other.longitude - longitude)

    val bx = cos(lat2) * cos(dLon)
    val by = cos(lat2) * sin(dLon)
    val midLat = atan2(sin(lat1) + sin(lat2), sqrt((cos(lat1) + bx).pow(2.0) + by.pow(2.0)))
    val midLon = lon1 + atan2(by, cos(lat1) + bx)
    val avgElev = (elevation + other.elevation) / 2.0
    return Coordinates(toDegrees(midLat), toDegrees(midLon), avgElev)
}

/** Next prayer reminder in human-friendly form (localized). */
fun PrayTimes.nextPrayerReminder(currentHour: Int, currentMinute: Int, locale: Locale = Locale.getDefault()): String {
    val currentMinutes = currentHour * 60 + currentMinute
    val times = listOf(
        "Fajr" to fajr,
        "Sunrise" to sunrise,
        "Dhuhr" to dhuhr,
        "Asr" to asr,
        "Maghrib" to maghrib,
        "Isha" to isha
    )
    for ((name, time) in times) {
        val minutes = (time * 60).roundToInt()
        if (minutes > currentMinutes) {
            val diff = minutes - currentMinutes
            val hours = diff / 60
            val mins = diff % 60
            return String.format(locale, "%s %s %dh %dm", "Next:", name, hours, mins)
        }
    }
    return "No more prayers today"
}

/** Utility: convert prayer times to simple list of pairs for UI lists. */
fun PrayTimes.toList(): List<Pair<String, String>> = toMapFormatted().toList()
 
