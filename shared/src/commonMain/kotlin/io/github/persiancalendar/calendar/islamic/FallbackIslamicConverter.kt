package io.github.persiancalendar.calendar.islamic

import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.DateTriplet
import io.github.persiancalendar.calendar.util.sinOfDegree
import io.github.persiancalendar.calendar.util.toRadians
import kotlin.math.cos
import kotlin.math.sin

internal object FallbackIslamicConverter {
    private const val NMONTHS = (1405 * 12) + 1
    private fun floor(d: Double): Long = kotlin.math.floor(d).toLong()

    fun toJdn(year: Int, month: Int, day: Int): Long {
        // NMONTH is the number of months between julian day number 1 and
        // the year 1405 A.H. which started immediatly after lunar
        // conjunction number 1048 which occured on September 1984 25d
        // 3h 10m UT.
        var year = year
        if (year < 0) year++
        val k = (month + year * 12 - NMONTHS).toLong() // nunber of months since 1/1/1405
        return floor(visibility(k + 1048) + day + .5)
    }

    private fun tmoonphase(n: Long, nph: Int): Double {
        val k = n + nph / 4.0
        val T = k / 1236.85
        val t2 = T * T
        val t3 = t2 * T
        val jd = (2415020.75933 + 29.53058868 * k - .0001178 * t2 - .000000155 * t3 + .00033
                * sinOfDegree(166.56 + 132.87 * T - .009173 * t2))

        // Sun's mean anomaly
        val sa = (359.2242 + 29.10535608 * k - .0000333 * t2 - .00000347 * t3).toRadians()

        // Moon's mean anomaly
        val ma = (306.0253 + 385.81691806 * k + .0107306 * t2 + .00001236 * t3).toRadians()

        // Moon's argument of latitude
        val tf = (
                2 * (21.2964 + 390.67050646 * k - .0016528 * t2 - .00000239 * t3)
                ).toRadians()
        val xtra = when (nph) {
            0, 2 -> (
                    ((.1734 - .000393 * T) * sin(sa)) +
                            (.0021 * sin(sa * 2)) -
                            (.4068 * sin(ma)) +
                            (.0161 * sin(2 * ma)) -
                            (.0004 * sin(3 * ma)) +
                            (.0104 * sin(tf))
                    ) - (.0051 * sin(sa + ma)) -
                    (.0074 * sin(sa - ma)) +
                    (.0004 * sin(tf + sa)) -
                    (.0004 * sin(tf - sa)) -
                    (.0006 * sin(tf + ma)) +
                    (.001 * sin(tf - ma)) +
                    (.0005 * sin(sa + 2 * ma))

            1, 3 -> {
                (
                        ((.1721 - .0004 * T) * sin(sa)) +
                                (.0021 * sin(sa * 2)) -
                                (.628 * sin(ma)) +
                                (.0089 * sin(2 * ma)) -
                                (.0004 * sin(3 * ma)) +
                                (.0079 * sin(tf))
                        ) -
                        (.0119 * sin(sa + ma)) -
                        (.0047 * sin(sa - ma)) +
                        (.0003 * sin(tf + sa)) -
                        (.0004 * sin(tf - sa)) -
                        (.0006 * sin(tf + ma)) +
                        (.0021 * sin(tf - ma)) +
                        (.0003 * sin(sa + 2 * ma)) +
                        (.0004 * sin(sa - 2 * ma)) -
                        (.0003 * sin(2 * sa + ma)) +
                        (if (nph == 1) .0028 - (.0004 * cos(sa)) + (.0003 * cos(ma))
                        else -.0028 + (.0004 * cos(sa)) - (.0003 * cos(ma)))
            }

            else -> return 0.0
        }
        // convert from Ephemeris Time (ET) to (approximate)Universal Time (UT)
        return jd + xtra - (.41 + (1.2053 * T) + (.4992 * t2)) / 1440
    }

    private fun visibility(n: Long): Double {
        // parameters for Makkah: for a new moon to be visible after sunset on
        // a the same day in which it started, it has to have started before
        // (SUNSET-MINAGE)-TIMZ=3 A.M. local time.
        val TIMZ = 3f
        val MINAGE = 13.5f
        val SUNSET = 19.5f
        // approximate
        val TIMDIF = SUNSET - MINAGE
        val jd = tmoonphase(n, 0)
        val d = floor(jd)
        var tf = jd - d
        return if (tf <= .5) // new moon starts in the afternoon
            jd + 1f else { // new moon starts before noon
            tf = (tf - .5) * 24 + TIMZ // local time
            if (tf > TIMDIF) jd + 1.0 // age at sunset < min for visiblity
            else jd
        }
    }

    fun fromJdn(jd: Long): DateTriplet {
        val civil = CivilDate(jd)
        var year = civil.year
        var month = civil.month
        var day = civil.dayOfMonth
        var k =
            floor(.6 + (year + (if (month % 2 == 0) month else month - 1) / 12.0 + day / 365f - 1900) * 12.3685)
        var mjd: Double
        do {
            mjd = visibility(k)
            k -= 1
        } while (mjd > jd - .5)
        k += 1
        val hm = k - 1048
        year = 1405 + (hm / 12).toInt()
        month = (hm % 12).toInt() + 1
        if (hm != 0L && month <= 0) {
            month += 12
            year -= 1
        }
        if (year <= 0) year -= 1
        day = floor(jd - mjd + .5).toInt()
        return DateTriplet(year = year, month = month, dayOfMonth = day)
    }
}
