// Ported from
// https://github.com/roozbehp/persiancalendar/blob/daf8fb2b46466a324cee98833c19c36aa5d97f39/persiancalendar.py
// Which is released under Apache 2.0 license
package io.github.persiancalendar.calendar.util

import io.github.persiancalendar.calendar.DateTriplet
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.tan

// https://github.com/unicode-org/icu4x/tree/6187c85412aa9154ac2d8e6f5310fa297ff0026b/utils/calendrical_calculations

/** The value of x shifted into the range [a..b). Returns x if a=b. */
private fun mod3(x: Double, a: Int, b: Int): Double =
    if (a == b) x else a + (x - a) % (b - a)

/** Sum powers of x with coefficients (from order 0 up) in list a. */
private fun poly(indeterminate: Double, coefficients: DoubleArray): Double {
    var sum = coefficients[0]
    var indeterminateRaised = 1.0
    for (i in 1..<coefficients.size) {
        indeterminateRaised *= indeterminate
        sum += coefficients[i] * indeterminateRaised
    }
    return sum
}

/**
 * Identity function for fixed dates/moments. If internal
 * timekeeping is shifted, change epoch to be RD date of
 * origin of internal count. epoch should be an integer.
 */
private fun rd(tee: Int): Int {
    val epoch = 0
    return tee - epoch
}

// Fixed date of start of the (proleptic) Gregorian calendar.
private val GREGORIAN_EPOCH = rd(1)

/**
 * True if g_year is a leap year on the Gregorian calendar.
 */
private fun gregorianLeapYear(gYear: Int): Boolean {
    return gYear % 4 == 0 && when (gYear % 400) {
        100, 200, 300 -> false
        else -> true
    }
}

/** Fixed date equivalent to the Gregorian date g_date. */
internal fun fixedFromGregorian(year: Int, month: Int, day: Int): Int {
    return (GREGORIAN_EPOCH - 1  // Days before start of calendar
            + 365 * (year - 1)  // Ordinary days since epoch
            + (year - 1).floorDiv(4)   // Julian leap days since epoch...
            - (year - 1).floorDiv(100)  // ...minus century years since epoch...
            + (year - 1).floorDiv(400)  // plus years since epoch divisible by 400.
            // Days in prior months this year assuming 30-day Feb
            + (367 * month - 362) / 12
            // Correct for 28- or 29-day Feb
            + (if (month <= 2) 0 else if (gregorianLeapYear(year)) -1 else -2)
            + day)  // Days so far this month.
}

/** Gregorian year corresponding to the fixed date. */
private fun gregorianYearFromFixed(date: Int): Int {
    val d0 = date - GREGORIAN_EPOCH  // Prior days.
    val n400 = d0 / 146097  // Completed 400-year cycles.
    val d1 = d0 % 146097  // Prior days not in n400.
    val n100 = d1 / 36524  // 100-year cycles not in n400.
    val d2 = d1 % 36524  // Prior days not in n400 or n100.
    val n4 = d2 / 1461  // 4-year cycles not in n400 or n100.
    val d3 = d2 % 1461  // Prior days not in n400, n100, or n4.
    val n1 = d3 / 365  // Years not in n400, n100, or n4.
    val year = 400 * n400 + 100 * n100 + 4 * n4 + n1
    return if (n100 == 4 || n1 == 4) {
        year  // Date is day 366 in a leap year.
    } else {
        year + 1  // Date is ordinal day (d % 365 + 1) in (year + 1).
    }
}

/** Fixed date of January 1 in g_year. */
private fun gregorianNewYear(gYear: Int): Int = fixedFromGregorian(gYear, 1, 1)

/** Gregorian (year, month, day) corresponding to fixed date. */
internal fun gregorianFromFixed(date: Int): DateTriplet {
    val year = gregorianYearFromFixed(date)
    val priorDays = date - gregorianNewYear(year)  // This year
    // To simulate a 30-day Feb
    val correction = when {
        date < fixedFromGregorian(year, 3, 1) -> 0
        gregorianLeapYear(year) -> 1
        else -> 2
    }
    val month = (12 * (priorDays + correction) + 373) / 367  // Assuming a 30-day Feb
    // Calculate the day by subtraction.
    val day = date - fixedFromGregorian(year, month, 1) + 1
    return DateTriplet(year = year, month = month, dayOfMonth = day)
}

/** Number of days from Gregorian date g_date1 until g_date2. */
private fun gregorianDateDifference(
    year1: Int, month1: Int, day1: Int,
    year2: Int, month2: Int, day2: Int,
): Int = fixedFromGregorian(year2, month2, day2) - fixedFromGregorian(year1, month1, day1)

// Fixed date of start of the Julian calendar.
private val JULIAN_EPOCH = fixedFromGregorian(0, 12, 30)

/** True if j_year is a leap year on the Julian calendar. */
private fun julianLeapYear(jYear: Int): Boolean = (jYear % 4) == (if (jYear > 0) 0 else 3)

/** Fixed date equivalent to the Julian date. */
internal fun fixedFromJulian(year: Int, month: Int, day: Int): Int {
    val y = if (year < 0) year + 1 else year  // No year zero
    return (JULIAN_EPOCH - 1  // Days before start of calendar
            + 365 * (y - 1)  // Ordinary days since epoch.
            + (y - 1).floorDiv(4)   // Leap days since epoch...
            // Days in prior months this year...
            + ((367 * month - 362) / 12)  // ...assuming 30-day Feb
            // Correct for 28- or 29-day Feb
            + (if (month <= 2) 0 else if (julianLeapYear(year)) -1 else -2)
            + day)           // Days so far this month.
}

/** True if jYear is a leap year on the Julian calendar. */
private fun isJulianLeapYear(jYear: Int): Boolean = jYear % 4 == (if (jYear > 0) 0 else 3)

/** Julian (year month day) corresponding to fixed $date$. */
internal fun julianFromFixed(date: Int): DateTriplet {
    // Nominal year.
    val approx = floor((4 * (date - JULIAN_EPOCH) + 1464) / 1461.0).toInt()
    val year = if (approx <= 0) approx - 1 else approx  // No year 0.

    // This year
    val priorDays = date - fixedFromJulian(year, 1, 1)

    // To simulate a 30-day Feb
    val correction = if (date < fixedFromJulian(year, 3, 1)) 0 else {
        if (isJulianLeapYear(year)) 1 else 2
    }

    // Assuming a 30-day Feb
    val month = floor((12 * (priorDays + correction) + 373) / 367.0).toInt()

    // Calculate the day by subtraction.
    val day = 1 + (date - fixedFromJulian(year, month, 1))

    return DateTriplet(year = year, month = month, dayOfMonth = day)
}

/** x hours. */
private fun hr(x: Int): Double = x / 24.0

/** d degrees, m arcminutes, s arcseconds. */
private fun angle(d: Int, m: Int, s: Double): Double = d + (m + s / 60) / 60.0

/** Convert angle theta from degrees to radians. */
private fun radiansFromDegrees(theta: Double): Double = (theta % 360) * PI / 180

/** Sine of theta (given in degrees). */
private fun sinDegrees(theta: Double): Double = sin(radiansFromDegrees(theta))

/** Cosine of theta (given in degrees). */
private fun cosDegrees(theta: Double): Double = cos(radiansFromDegrees(theta))

/** Tangent of theta (given in degrees). */
private fun tanDegrees(theta: Double): Double = tan(radiansFromDegrees(theta))

/**
 * Difference between UT and local mean time at longitude
 * phi as a fraction of a day.
 */
private fun zoneFromLongitude(phi: Double): Double = phi / 360

/** Universal time from local tee_ell at location */
private fun universalFromLocal(teeEll: Double, longitude: Double): Double =
    teeEll - zoneFromLongitude(longitude)

/** Local time from sundial time tee at location. */
private fun localFromApparent(tee: Double, longitude: Double): Double =
    tee - equationOfTime(universalFromLocal(tee, longitude))

/** Universal time from sundial time tee at location */
private fun universalFromApparent(tee: Double, longitude: Double): Double =
    universalFromLocal(localFromApparent(tee, longitude), longitude)

/** Universal time on fixed date of midday at location */
private fun midday(date: Int, longitude: Double): Double =
    universalFromApparent(date + hr(12), longitude)

/** Julian centuries since 2000 at moment tee. */
private fun julianCenturies(tee: Double): Double =
    (dynamicalFromUniversal(tee) - J2000) / 36525

private val obliquityCooefficients = doubleArrayOf(
    0.0,
    angle(0, 0, -46.8150),
    angle(0, 0, -0.00059),
    angle(0, 0, 0.001813),
)

/** Obliquity of ecliptic at moment tee. */
private fun obliquity(tee: Double): Double {
    val c = julianCenturies(tee)
    return angle(23, 26, 21.448) + poly(c, obliquityCooefficients)
}

/** Dynamical time at Universal moment tee_rom_u. */
private fun dynamicalFromUniversal(teeRomU: Double): Double =
    teeRomU + ephemerisCorrection(teeRomU)

// Noon at start of Gregorian year 2000.
private val J2000 = hr(12) + gregorianNewYear(2000)

private const val MEAN_TROPICAL_YEAR = 365.242189

private val c2006Coefficients = doubleArrayOf(62.92, 0.32217, 0.005589)
private val c1987Coefficients = doubleArrayOf(
    63.86, 0.3345, -0.060374,
    0.0017275,
    0.000651814, 0.00002373599
)
private val c1900Coefficients = doubleArrayOf(
    -0.00002, 0.000297, 0.025184,
    -0.181133, 0.553040, -0.861938,
    0.677066, -0.212591
)
private val c1800Coefficients = doubleArrayOf(
    -0.000009, 0.003844, 0.083563,
    0.865736,
    4.867575, 15.845535, 31.332267,
    38.291999, 28.316289, 11.636204,
    2.043794
)
private val c1700Coefficients = doubleArrayOf(
    8.118780842, -0.005092142,
    0.003336121, -0.0000266484
)
private val c1600Coefficients = doubleArrayOf(
    120.0, -0.9808, -0.01532,
    0.000140272128
)
private val c500Coefficients = doubleArrayOf(
    1574.2, -556.01, 71.23472, 0.319781,
    -0.8503463, -0.005050998,
    0.0083572073
)
private val c0Coefficients = doubleArrayOf(
    10583.6, -1014.41, 33.78311,
    -5.952053, -0.1798452, 0.022174192,
    0.0090316521
)
private val otherCoefficients = doubleArrayOf(-20.0, 0.0, 32.0)

/**
 * Dynamical Time minus Universal Time (in days) for moment tee.
 *
 * Adapted from "Astronomical Algorithms"
 * by Jean Meeus, Willmann-Bell (1991) for years
 * 1600-1986 and from polynomials on the NASA
 * Eclipse web site for other years.
 */
private fun ephemerisCorrection(tee: Double): Double {
    val year = gregorianYearFromFixed(floor(tee).toInt())
    return when {
        year in 2051..2150 ->
            (-20 + 32 * ((year - 1820) / 100.0).pow(2) + 0.5628 * (2150 - year)) / 86400

        year in 2006..2050 -> {
            val y2000 = year - 2000
            poly(y2000.toDouble(), c2006Coefficients) / 86400
        }

        year in 1987..2005 -> {
            val y2000 = year - 2000
            poly(y2000.toDouble(), c1987Coefficients) / 86400
        }

        year in 1900..1986 -> {
            val c = gregorianDateDifference(1900, 1, 1, year, 7, 1) / 36525.0
            poly(c, c1900Coefficients)
        }

        year in 1800..1899 -> {
            val c = gregorianDateDifference(1900, 1, 1, year, 7, 1) / 36525.0
            poly(c, c1800Coefficients)
        }

        year in 1700..1799 -> {
            val y1700 = year - 1700
            poly(y1700.toDouble(), c1700Coefficients) / 86400
        }

        year in 1600..1699 -> {
            val y1600 = year - 1600
            poly(y1600.toDouble(), c1600Coefficients) / 86400
        }

        year in 500..1599 -> {
            val y1000 = (year - 1000) / 100.0
            poly(y1000, c500Coefficients) / 86400
        }

        -500 < year && year < 500 -> {
            val y0 = year / 100.0
            poly(y0, c0Coefficients) / 86400
        }

        else -> {
            val y1820 = (year - 1820) / 100.0
            poly(y1820, otherCoefficients) / 86400
        }
    }
}

private val lamdaCoefficient = doubleArrayOf(280.46645, 36000.76983, 0.0003032)
private val anamolyCoefficients = doubleArrayOf(357.52910, 35999.05030, -0.0001559, -0.00000048)
private val eccentricityCoefficients = doubleArrayOf(0.016708617, -0.000042037, -0.0000001236)

/**
 * Equation of time (as fraction of day) for moment tee.
 *
 * Adapted from "Astronomical Algorithms" by Jean Meeus,
 * Willmann-Bell, 2nd edn., 1998, p. 185.
 */
private fun equationOfTime(tee: Double): Double {
    val c = julianCenturies(tee)
    val lamda = poly(c, lamdaCoefficient)
    val anomaly =
        poly(c, anamolyCoefficients)
    val eccentricity = poly(c, eccentricityCoefficients)
    val varepsilon = obliquity(tee)
    val y = tanDegrees(varepsilon / 2).pow(2)
    val equation = ((1.0 / 2 / PI) *
            (y * sinDegrees(2 * lamda)
                    - 2 * eccentricity * sinDegrees(anomaly)
                    + 4 * eccentricity * y * sinDegrees(anomaly)
                    * cosDegrees(2 * lamda)
                    - 0.5 * y * y * sinDegrees(4 * lamda)
                    - 1.25 * eccentricity * eccentricity
                    * sinDegrees(2 * anomaly)))
    return sign(equation) * min(abs(equation), hr(12))
}

private val solarLongitudeCoefficients = intArrayOf(
    403406, 195207, 119433, 112392, 3891, 2819, 1721,
    660, 350, 334, 314, 268, 242, 234, 158, 132, 129, 114,
    99, 93, 86, 78, 72, 68, 64, 46, 38, 37, 32, 29, 28, 27, 27,
    25, 24, 21, 21, 20, 18, 17, 14, 13, 13, 13, 12, 10, 10, 10,
    10
)
private val solarLongitudeMultipliers = doubleArrayOf(
    0.9287892, 35999.1376958, 35999.4089666,
    35998.7287385, 71998.20261, 71998.4403,
    36000.35726, 71997.4812, 32964.4678,
    -19.4410, 445267.1117, 45036.8840, 3.1008,
    22518.4434, -19.9739, 65928.9345,
    9038.0293, 3034.7684, 33718.148, 3034.448,
    -2280.773, 29929.992, 31556.493, 149.588,
    9037.750, 107997.405, -4444.176, 151.771,
    67555.316, 31556.080, -4561.540,
    107996.706, 1221.655, 62894.167,
    31437.369, 14578.298, -31931.757,
    34777.243, 1221.999, 62894.511,
    -4442.039, 107997.909, 119.066, 16859.071,
    -4.578, 26895.292, -39.127, 12297.536,
    90073.778
)
private val solarLongitudeAddends = doubleArrayOf(
    270.54861, 340.19128, 63.91854, 331.26220,
    317.843, 86.631, 240.052, 310.26, 247.23,
    260.87, 297.82, 343.14, 166.79, 81.53,
    3.50, 132.75, 182.95, 162.03, 29.8,
    266.4, 249.2, 157.6, 257.8, 185.1, 69.9,
    8.0, 197.1, 250.4, 65.3, 162.7, 341.5,
    291.6, 98.5, 146.7, 110.0, 5.2, 342.6,
    230.9, 256.1, 45.3, 242.9, 115.2, 151.8,
    285.3, 53.3, 126.6, 205.7, 85.9,
    146.1
)

/**
 * Longitude of sun at moment tee.
 *
 * Adapted from "Planetary Programs and Tables from -4000
 * to +2800" by Pierre Bretagnon and Jean-Louis Simon,
 * Willmann-Bell, 1986.
 */
private fun solarLongitude(tee: Double): Double {
    val c = julianCenturies(tee)  // moment in Julian centuries
    val lamda = (
            282.7771834
                    + 36000.76953744 * c
                    + 0.000005729577951308232 *
                    solarLongitudeCoefficients.indices.sumOf { i ->
                        solarLongitudeCoefficients[i] * sinDegrees(
                            solarLongitudeAddends[i] + solarLongitudeMultipliers[i] * c
                        )
                    }
            )
    return (lamda + aberration(tee) + nutation(tee)).mod(360.0)
}

private val nutationCoefficientA = doubleArrayOf(124.90, -1934.134, 0.002063)
private val nutationCoefficientB = doubleArrayOf(201.11, 72001.5377, 0.00057)

/** Longitudinal nutation at moment tee. */
private fun nutation(tee: Double): Double {
    val c = julianCenturies(tee)  // moment in Julian centuries
    val capA = poly(c, nutationCoefficientA)
    val capB = poly(c, nutationCoefficientB)
    return -0.004778 * sinDegrees(capA) - 0.0003667 * sinDegrees(capB)
}

/** Aberration at moment tee. */
private fun aberration(tee: Double): Double {
    val c = julianCenturies(tee)  // moment in Julian centuries
    return 0.0000974 * cosDegrees(177.63 + 35999.01848 * c) - 0.005575
}

// Longitude of sun at vernal equinox.
private const val SPRING = 0.0

/**
 * Approximate moment at or before tee
 * when solar longitude just exceeded lamda degrees.
 */
private fun estimatePriorSolarLongitude(lamda: Double, tee: Double): Double {
    val rate = MEAN_TROPICAL_YEAR / 360  // Mean change of one degree.
    // First approximation.
    val tau = tee - rate * ((solarLongitude(tee) - lamda).mod(360.0))
    val capDelta = mod3((solarLongitude(tau) - lamda), -180, 180)
    return min(tee, tau - rate * capDelta)
}

// Fixed date of start of the Persian calendar.
private val PERSIAN_EPOCH = fixedFromJulian(622, 3, 19)

// Location of Tehran, Iran.
// Specifically location of "Dar ul-Funun", https://w.wiki/DjPM
private val TEHRAN = doubleArrayOf(35.683789, 51.421864, 1100.0, +3.5)

// Middle of Iran.
private val IRAN = doubleArrayOf(35.5, 52.5, 0.0, +3.5)

/** Fixed date of Astronomical Persian New Year on or before fixed date. */
internal fun persianNewYearOnOrBefore(date: Int, longitude: Double): Int {
    // Approximate time of equinox.
    val approx = estimatePriorSolarLongitude(SPRING, midday(date, longitude))
    var day = floor(approx).toInt() - 1
    while (solarLongitude(midday(day, longitude)) > SPRING + 2) day += 1
    return day
}

/** Fixed date of Borji Persian new month on or before fixed date. */
internal fun persianBorjiNewMonthOnOrBefore(date: Int, month: Int, longitude: Double): Int {
    // Approximate time of equinox.
    val targetLong = (month - 1) * 30.0
    val approx = estimatePriorSolarLongitude(targetLong, midday(date, longitude))
    var day = floor(approx).toInt() - 1
    while (true) {
        val solarLong = solarLongitude(midday(day, longitude))
        if ((targetLong + 2 > solarLong && solarLong >= targetLong)) break
        day += 1
    }
    return day
}

/** Fixed date of Astronomical Persian date p_date. */
internal fun fixedFromPersian(year: Int, month: Int, day: Int, longitude: Double): Int {
    val newYear = persianNewYearOnOrBefore(
        PERSIAN_EPOCH + 180  // Fall after epoch.
                + floor(
            MEAN_TROPICAL_YEAR *
                    (if (0 < year) year - 1 else year)
        ).toInt(),
        longitude
    )  // No year zero.
    return (newYear - 1  // Days in prior years.
            // Days in prior months this year.
            + (if (month <= 7) 31 * (month - 1) else 30 * (month - 1) + 6)
            + day)  // Days so far this month.
}

/** Fixed date of Borji Persian date p_date. */
internal fun fixedFromPersianBorji(year: Int, month: Int, day: Int, longitude: Double): Int {
    val newMonth = persianBorjiNewMonthOnOrBefore(
        PERSIAN_EPOCH + 180
                + floor(
            MEAN_TROPICAL_YEAR *
                    ((if (0 < year) year - 1 else year) + (month - 1) / 12.0)
        ).toInt(),
        month,
        longitude
    )
    return (newMonth - 1  // Days in prior months.
            + day)  // Days so far this month.
}

/** Astronomical Persian date corresponding to fixed date. */
internal fun persianFromFixed(date: Int, longitude: Double): DateTriplet {
    val newYear = persianNewYearOnOrBefore(date, longitude)
    val y = round((newYear - PERSIAN_EPOCH) / MEAN_TROPICAL_YEAR).toInt() + 1
    val year = if (0 < y) y else y - 1  // No year zero
    val dayOfYear = date - fixedFromPersian(year, 1, 1, longitude) + 1
    val month =
        if (dayOfYear <= 186) ceil(dayOfYear / 31.0).toInt()
        else ceil((dayOfYear - 6) / 30.0).toInt()
    // Calculate the day by subtraction
    val day = date - fixedFromPersian(year, month, 1, longitude) + 1
    return DateTriplet(year = year, month = month, dayOfMonth = day)
}

/** Borji Persian date corresponding to fixed date. */
internal fun persianBorjiFromFixed(date: Int, longitude: Double): DateTriplet {
    val newYear = persianNewYearOnOrBefore(date, longitude)
    val y = round((newYear - PERSIAN_EPOCH) / MEAN_TROPICAL_YEAR).toInt() + 1
    val year = if (0 < y) y else y - 1  // No year zero
    var month = 1
    while (month < 12 && date >= fixedFromPersianBorji(year, month + 1, 1, longitude)) {
        month += 1
    }
    // Calculate the day by subtraction
    val day = date - fixedFromPersianBorji(year, month, 1, longitude) + 1
    return DateTriplet(year = year, month = month, dayOfMonth = day)
}

/** Fixed date of Persian New Year (Nowruz) in Gregorian year g_year. */
private fun nowruz(gYear: Int, longitude: Double): Int {
    val persianYear = gYear - gregorianYearFromFixed(PERSIAN_EPOCH) + 1
    val y = if (persianYear <= 0) persianYear - 1 else persianYear  // No Persian year 0
    return fixedFromPersian(y, 1, 1, longitude)
}

/** True if year is a leap year on the Persian calendar. */
internal fun persianLeapYear(year: Int, longitude: Double): Boolean {
    val thisNowruz = fixedFromPersian(year, 1, 1, longitude)
    val nextNowruz = fixedFromPersian(year + 1, 1, 1, longitude)
    return nextNowruz - thisNowruz == 366
}

private const val OFFSET_JDN = 1_721_425L
private const val START_OF_MODERN_ERA_JDN = 2424231 // PersianDate(1304, 1, 1).toJdn()
private const val START_OF_MODERN_ERA_YEAR = 1304
internal fun persianFromJdn(jdn: Long, alwaysBorji: Boolean = false): DateTriplet {
    val isModernEra = jdn >= START_OF_MODERN_ERA_JDN
    val fixed = (jdn - OFFSET_JDN).toInt()
    val longitude = (if (isModernEra) IRAN else TEHRAN)[1]
    return if (isModernEra && !alwaysBorji) persianFromFixed(fixed, longitude)
    else persianBorjiFromFixed(fixed, longitude)
}

internal fun jdnFromPersian(year: Int, month: Int, dayOfMonth: Int): Long {
    val isModernEra = year >= START_OF_MODERN_ERA_YEAR
    val longitude = (if (isModernEra) IRAN else TEHRAN)[1]
    val fixed = if (isModernEra) fixedFromPersian(year, month, dayOfMonth, longitude)
    else fixedFromPersianBorji(year, month, dayOfMonth, longitude)
    return fixed.toLong() + OFFSET_JDN
}

// Expose this so the app can display Julian date for now
// but it's unstable and maybe we would decide to expose it differently
fun julianFromJdn(jdn: Long): DateTriplet = julianFromFixed((jdn - OFFSET_JDN).toInt())

internal fun jdnFromCivil(year: Int, month: Int, dayOfMonth: Int): Long {
    return OFFSET_JDN + if (
        (year > 1582)
        || ((year == 1582) && (month > 10))
        || ((year == 1582) && (month == 10) && (dayOfMonth > 14))
    ) fixedFromGregorian(year, month, dayOfMonth) else fixedFromJulian(year, month, dayOfMonth)
}

internal fun civilFromJdn(jdn: Long): DateTriplet =
    if (jdn > 2299160) gregorianFromFixed((jdn - OFFSET_JDN).toInt()) else julianFromJdn(jdn)
