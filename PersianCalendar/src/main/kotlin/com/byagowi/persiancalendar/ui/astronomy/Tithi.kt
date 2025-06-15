/**
 * This is ported from https://github.com/echeran/calendar-code2/blob/1de3bfdbcbe0a4353adb93ac73d706847d946ac8/clj/clj-calcalc/src/clj_calcalc/core.clj#L5474
 * The original code is under Apache 2.0 license and is already mentioned in the credits also
 */
package com.byagowi.persiancalendar.ui.astronomy

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.time.Duration.Companion.days

object Tithi {
    /**
     * TYPE (integer integer) -> (integer integer)
     * Euclidean division with remainder
     */
    private fun divEuclid(n: Int, d: Int): Int {
        val a = n / d
        val b = n % d
        return if (n >= 0 || b == 0) a else a - 1
    }

    /**
     * TYPE (integer integer) -> integer
     * Quotient of m divided by n
     */
    private fun quotient(m: Int, n: Int): Int = divEuclid(m, n)

    // TYPE rational
    // Mean length of Hindu sidereal year.
    private const val hinduSiderealYear: Double = 365.0 + 279457.0 / 1080000.0

    // TYPE fixed-date
    // Fixed date of start of the Hindu calendar (Kali Yuga).
    // (fixed-from-julian (julian-date (bce 3102) february 18))
    private const val hinduEpoch: Int = -1132959

    // TYPE fixed-date
    // Fixed date of Hindu creation.
    private val hinduCreation: Double = hinduEpoch - 1955880000 * hinduSiderealYear

    /**
     * TYPE (rational-moment rational) -> rational-angle
     * Position in degrees at moment tee in uniform circular orbit of period days.
     */
    private fun hinduMeanPosition(tee: Double, period: Double): Double =
        360.0 * ((tee - hinduCreation) / period % 1)

    /**
     * TYPE real -> radian
     * Convert angle theta from degrees to radians.
     */
    private fun radiansFromDegrees(theta: Double): Double = (theta % 360) * PI / 180.0

    /**
     * TYPE angle -> amplitude
     * Sine of theta (given in degrees).
     */
    private fun sinDegrees(theta: Double): Double = sin(radiansFromDegrees(theta))

    /**
     * TYPE (integer integer real) -> angle
     * d degrees, m arcminutes, s arcseconds.
     */
    private fun angle(d: Int, m: Int, s: Double): Double = d + (m + s / 60) / 60.0

    /**
     * TYPE integer -> rational-amplitude
     * This simulates the Hindu sine table.
     * entry is an angle given as a multiplier of 225'.
     */
    private fun hinduSineTable(entry: Int): Double {
        val exact = 3438.0 * sinDegrees(entry * angle(0, 225, 0.0))
        val error = 0.215 * sign(exact) * sign(abs(exact) - 1716)
        return round(exact + error) / 3438.0
    }

    /**
     * TYPE rational-angle -> rational-amplitude
     * Linear interpolation for theta in Hindu table.
     */
    private fun hinduSine(theta: Double): Double {
        val entry = theta / angle(0, 225, 0.0)  // Interpolate in table
        val fraction = entry % 1
        return fraction * hinduSineTable(ceil(entry).toInt()) +
                (1 - fraction) * hinduSineTable(floor(entry).toInt())
    }

    /**
     * TYPE (* integer (integer->boolean)) -> integer
     * First integer greater or equal to initial such that condition holds.
     */
    private fun next(initial: Int, condition: (Int) -> Boolean): Int {
        var index = initial
        while (!condition(index)) index++
        return index
    }

    /**
     * TYPE rational-amplitude -> rational-angle
     * Inverse of Hindu sine function of amp.
     */
    private fun hinduArcsin(amp: Double): Double {
        if (amp < 0) return -hinduArcsin(-amp)

        val pos = next(0) { k -> amp <= hinduSineTable(k) }
        val below = hinduSineTable(pos - 1)  // Lower value in table
        return angle(0, 225, 0.0) * (pos - 1 +  // Interpolate
                (amp - below) / (hinduSineTable(pos) - below))
    }

    /**
     * TYPE (rational-moment rational rational rational rational) -> rational-angle
     * Longitudinal position at moment tee. period is period of mean motion in days.
     * size is ratio of radii of epicycle and deferent. anomalistic is the period
     * of retrograde revolution about epicycle. change is maximum decrease in epicycle size.
     */
    private fun hinduTruePosition(
        tee: Double,
        period: Double,
        size: Double,
        anomalistic: Double,
        change: Double
    ): Double {
        val lambda = hinduMeanPosition(tee, period)  // Position of epicycle center
        val offset = hinduSine(hinduMeanPosition(tee, anomalistic))  // Sine of anomaly
        val contraction = abs(offset) * change * size
        val equation = hinduArcsin(offset * (size - contraction))  // Equation of center
        return (lambda - equation) % 360.0
    }

    // TYPE rational
    // Mean length of Hindu sidereal month.
    private const val hinduSiderealMonth: Double = 27.0 + 4644439.0 / 14438334.0

    // TYPE rational
    // Time from apogee to apogee, with bija correction.
    private val hinduAnomalisticMonth: Double = 1577917828.0 / (57753336.0 - 488199.0)

    /**
     * TYPE rational-moment -> rational-angle
     * Lunar longitude at moment tee.
     */
    private fun hinduLunarLongitude(tee: Double): Double {
        return hinduTruePosition(
            tee, hinduSiderealMonth,
            32.0 / 360.0, hinduAnomalisticMonth, 1.0 / 96.0
        )
    }

    // TYPE rational
    // Time from aphelion to aphelion.
    private val hinduAnomalisticYear: Double = 1577917828000.0 / (4320000000.0 - 387.0)

    /**
     * TYPE rational-moment -> rational-angle
     * Solar longitude at moment tee.
     */
    private fun hinduSolarLongitude(tee: Double): Double {
        return hinduTruePosition(
            tee, hinduSiderealYear,
            14.0 / 360.0, hinduAnomalisticYear, 1.0 / 42.0
        )
    }

    /**
     * TYPE rational-moment -> rational-angle
     * Longitudinal distance between the sun and moon at moment tee.
     */
    private fun hinduLunarPhase(tee: Double): Double =
        (hinduLunarLongitude(tee) - hinduSolarLongitude(tee)).mod(360.0)

    /**
     * TYPE rational-moment -> hindu-lunar-day
     * Phase of moon (tithi) at moment tee, as an integer in the range 1..30.
     */
    fun hinduLunarDayFromMoment(tee: Double): Int =
        1 + quotient(hinduLunarPhase(tee).toInt(), 12)

    private val tithiNamesInNepali = listOf(
        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी (चौथी)", "पञ्चमी", "षष्ठी", "सप्तमी", "अष्टमी",
        "नवमी", "दशमी", "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पूर्णिमा",

        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी (चौथी)", "पञ्चमी", "षष्ठी", "सप्तमी", "अष्टमी",
        "नवमी", "दशमी", "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "अमावश्या (औंसी)"
    )

    fun tithiName(timeMillis: Long): String {
        // Please note that (fixed-from-gregorian [2025 4 27]) => 739368
        val tee = 719_162 + timeMillis.toDouble() / 1.days.inWholeMilliseconds
        return tithiNamesInNepali.getOrNull(hinduLunarDayFromMoment(tee) - 1).orEmpty()
    }
}
