package com.byagowi.persiancalendar.utils

import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

// https://github.com/emvakar/EKAstrologyCalc/blob/a0acca1/Sources/EKAstrologyCalc/Calculators/EKEclipseCalculator.swift
// MIT Licensed per https://github.com/emvakar/EKAstrologyCalc/blob/a0acca1/EKAstrologyCalc.podspec#L10
/**
 * Gets next or previous eclipse info nearest to the reference julian day
 * - param jd julian day
 * - param eclipseType type of eclipse: Eclipse.SOLAR or Eclipse.LUNAR
 * - param next true to get next eclipse, false to get previous
 */
class Eclipse(date: GregorianCalendar, eclipseCategory: Category, next: Boolean) {

    enum class Category(val rawValue: Int) { SOLAR(0), LUNAR(1) }

    enum class Type {
        SolarNoncenral, SolarPartial, SolarCentralTotal, SolarCentralAnnular, SolarCentralAnnularTotal,
        LunarUmbralTotal, LunarUmbralPartial, LunarPenumbral
    }

    init {
        // AFFC, p. 32, f. 32.2
        var k = floor((dayToYear(date) - 1900) * 12.3685)
        k = if (next) k + eclipseCategory.rawValue * 0.5 else k - eclipseCategory.rawValue * 0.5

        var eclipseFound: Boolean
        do {
            // AFFC, p. 128, f. 32.3
            val T = k / 1236.85
            val TT = T * T
            val TTT = T * TT

            // Moon's argument of latitude
            // AFFC, p. 129
            val F = toRadians(
                to360(
                    21.2964 + 390.67050646 * k
                            - 0.0016528 * TT
                            - 0.00000239 * TTT
                )
            )

            // AFFC, p. 132
            eclipseFound = abs(sin(F)) < 0.36

            // no eclipse exactly, examine other lunation
            if (!eclipseFound) {
                k += if (next) 1 else -1
                continue
            }

            // BOTH ECLIPSE TYPES (SOLAR & LUNAR)

            // mean anomaly of the Sun
            // AFFC, p. 129
            val M = toRadians(
                (359.2242 + 29.10535608 * k
                        - 0.0000333 * TT
                        - 0.00000347 * TTT)
            )

            // mean anomaly of the Moon
            // AFFC, p. 129
            val M_ = toRadians(
                to360(
                    306.0253 + 385.81691806 * k
                            + 0.0107306 * TT
                            + 0.00001236 * TTT
                )
            )

            // time of mean phase
            // AFFC, p. 128, f. 32.1
            var timeByJulianDate = (2415020.75933 + 29.53058868 * k
                    + 0.0001178 * TT
                    - 0.000000155 * TTT
                    + 0.00033 *
                    sin(toRadians(166.56 + 132.87 * T - 0.009173 * TT)))

            // time of maximum eclipse
            timeByJulianDate += ((0.1734 - 0.000393 * T) * sin(M)
                    + 0.0021 * sin(M + M)
                    - 0.4068 * sin(M_)
                    + 0.0161 * sin(M_ + M_)
                    - 0.0051 * sin(M + M_)
                    - 0.0074 * sin(M - M_)
                    - 0.0104 * sin(F + F))

            maxPhaseDate = dateFrom(timeByJulianDate)

            // AFFC, p. 133
            val S = (5.19595
                    - 0.0048 * cos(M)
                    + 0.0020 * cos(M + M)
                    - 0.3283 * cos(M_)
                    - 0.0060 * cos(M + M_)
                    + 0.0041 * cos(M - M_))

            var C = (0.2070 * sin(M)
                    + 0.0024 * sin(M + M)
                    - 0.0390 * sin(M_)
                    + 0.0115 * sin(M_ + M_)
                    - 0.0073 * sin(M + M_)
                    - 0.0067 * sin(M - M_)
                    + 0.0117 * sin(F + F))

            gamma = S * sin(F) + C * cos(F)

            u = (0.0059
                    + 0.0046 * cos(M)
                    - 0.0182 * cos(M_)
                    + 0.0004 * cos(M_ + M_)
                    - 0.0005 * cos(M + M_))

            // SOLAR ECLIPSE
            if (eclipseCategory == Category.SOLAR) {
                // eclipse is not observable from the Earth
                if (abs(gamma) > 1.5432 + u) {
                    eclipseFound = false
                    k += if (next) 1 else -1
                    continue
                }

                // AFFC, p. 134
                // non-central eclipse
                if (abs(gamma) > 0.9972 && abs(gamma) < 0.9972 + abs(u)) {
                    type = Type.SolarNoncenral
                    phase = 1.0
                } else { // central eclipse
                    phase = 1.0
                    if (u < 0) {
                        type = Type.SolarCentralTotal
                    }
                    if (u > 0.0047) {
                        type = Type.SolarCentralAnnular
                    }
                    if (u > 0 && u < 0.0047) {
                        C = 0.00464 * sqrt(1 - gamma * gamma)
                        type =
                            if (u < C) Type.SolarCentralAnnularTotal else Type.SolarCentralAnnular
                    }
                }

                // partial eclipse
                if (abs(gamma) > 0.9972 && abs(gamma) < 1.5432 + u) {
                    type = Type.SolarPartial
                    phase = (1.5432 + u - abs(gamma)) / (0.5461 + u + u)
                }
            } else { // LUNAR ECLIPSE
                rho = 1.2847 + u
                sigma = 0.7494 - u

                // Phase for umbral eclipse
                // AFFC, p. 135, f. 33.4
                phase = (1.0129 - u - abs(gamma)) / 0.5450

                if (phase >= 1) {
                    type = Type.LunarUmbralTotal
                }

                if (phase > 0 && phase < 1) {
                    type = Type.LunarUmbralPartial
                }

                // Check if elipse is penumral only
                if (phase < 0) {
                    // AFC, p. 135, f. 33.3
                    type = Type.LunarPenumbral
                    phase = (1.5572 + u - abs(gamma)) / 0.5450
                }

                // no eclipse, if both phases is less than 0,
                // then examine other lunation
                if (phase < 0) {
                    eclipseFound = false

                    k += if (next) 1 else -1
                    continue
                }

                // eclipse was found, calculate remaining details
                // AFFC, p. 135
                val P = 1.0129 - u
                val Tau = 0.4679 - u
                val n = 0.5458 + 0.0400 * cos(M_)

                // semiduration in penumbra
                C = u + 1.5573
                sdPenumbra = 60.0 / n * sqrt(C * C - gamma * gamma)

                // semiduration of partial phase
                sdPartial = 60.0 / n * sqrt(P * P - gamma * gamma)

                // semiduration of total phase
                sdTotal = 60.0 / n * sqrt(Tau * Tau - gamma * gamma)
            }
        } while (!eclipseFound)
    }

    /** UTC date & time of maximal phase of eclipse (for Earth center) */
    lateinit var maxPhaseDate: Date
        private set

    /** Maximal phase of eclipse */
    var phase: Double = 0.0
        private set

    /** Type of eclipse */
    lateinit var type: Type
        private set

    /** Minimal distance between:
    a) solar eclipse: center of Moon shadow axis and Earth center
    b) lunar eclipse: Moon center and Earth shadow axis. */
    var gamma: Double = 0.0
        private set

    /** Radius of ...  */
    var u: Double = 0.0
        private set

    // /** Eclipse visibility for local point */
    // var visibility: Int = 0
    //     private set

    // /** Julian date for observable phase */
    // var jdBestVisible: Double = 0.0
    //     private set

    // LUNAR ECLIPSE

    /** Penumra radius (in Earth equatorial radii) */
    var rho: Double? = null
        private set

    /** Umbra radius (in Earth equatorial radii) */
    var sigma: Double? = null
        private set

    /** Semiduration of partial phase in penumbra, in minutes */
    var sdPenumbra: Double? = null
        private set

    /** Semiduration of partial phase, in minutes */
    var sdPartial: Double? = null
        private set

    /** Semiduration of total phase, in minutes */
    var sdTotal: Double? = null
        private set

    // SOLAR ECLIPSE

    // /** Maximal local phase of eclipse */
    // var phaseLocal: Double? = null
    //     private set

    // /** UTC date & time eclipse maximum for local point */
    // var jdLocal: Double? = null
    //     private set

    // /** Time of partial phase beginning for local point */
    // var jdLocalPartialStart: Double? = null
    //     private set

    // /** Time of partial phase ending for local point */
    // var jdLocalPartialEnd: Double? = null
    //     private set

    companion object {
        // Converts date to year with fractions
        private fun dayToYear(date: GregorianCalendar): Double =
            date[Calendar.YEAR] + date[Calendar.DAY_OF_YEAR] / 365.2425

        /// returns gregorian date from julian calendar date timestamp
        private fun dateFrom(julianTime: Double) =
            Date(((julianTime - 2440587.5/*JD_JAN_1_1970_0000GMT*/) * 86400000).toLong())

        private fun to360(angle: Double) = angle % 360.0 + if (angle < 0) 360 else 0

        /// convert angle to radians
        private fun toRadians(angle: Double) = angle * Math.PI / 180

        // Local visibility circumstances (lunar & solar both)
        // val VISIBILITY_NONE = 0
        // val VISIBILITY_PARTIAL = 1
        // val VISIBILITY_FULL = 2
        // val VISIBILITY_START_PENUMBRA = 3
        // val VISIBILITY_START_PARTIAL = 4
        // val VISIBILITY_START_FULL = 5
        // val VISIBILITY_END_FULL = 6
        // val VISIBILITY_END_PARTIAL = 7
        // val VISIBILITY_END_PENUMBRA = 8
    }
}
