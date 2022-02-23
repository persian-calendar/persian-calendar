package com.cepmuvakkit.times.posAlgo

import com.cepmuvakkit.times.posAlgo.MoonPhases.searchPhaseEvent
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlin.math.tan

object AstroLib {
    // private static final int JGREG = 588829;//15 + 31*(10+12*1582); //
    // Gregorian Calendar adopted Oct. 15, 1582 (2299161)
    // Gregorian Calendar adopted Oct. 15, 1582 (2299161)
    /**
     * The Astrolib date starts on Jaνary 1, in the year - 4712 at 12:00:00 UT.
     * The Astrolib Day (JD) is calculated using UT and the Astrolib Ephemeris
     * Day (JDE) is calculated using TT. In the following steps, note that there
     * is a 10-day gap between the Astrolib and Gregorian calendar where the
     * Astrolib calendar ends on October 4, 1582 (JD = 2299160), and after
     * 10-days the Gregorian calendar starts on October 15, 1582
     *
     * @param year  is the year (e.g. 2001, 2002, ..etc.).
     * @param month is the month of the year (e.g. 1 for January, ..etc.). Note
     * that if M > 2, then Y and M are not changed, but if M = 1 or
     * 2, then Y = Y-1 and M = M + 12.
     * @param day   is the day of the month with decimal time (e.g. for the second
     * day of the month at 12:30:30 UT, D = 2.521180556).
     * @param hour  is the hour
     * @param tz    is the timezone (if timezone not given that means you assuming
     * greenwich timezone)
     * @return Calculate the Astrolib Day ( Julian date independent of time zone
     * by the way)
     */
    fun calculateJulianDay(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int, second: Int, tz: Double
    ): Double {
        var year = year
        var month = month
        val dayDecimal = day + (hour - tz + (minute + second / 60.0) / 60.0) / 24.0
        if (month < 3) {
            month += 12
            year--
        }
        var julianDay = (floor(365.25 * (year + 4716.0))
                + floor(30.6001 * (month + 1)) + dayDecimal) - 1524.5
        if (julianDay > 2299160.0) {
            val a = floor((year / 100).toDouble())
            julianDay += 2 - a + floor(a / 4)
        }
        return julianDay
    }

    /**
     * The Astrolib date starts on Jaνary 1, in the year - 4712 at 12:00:00 UT.
     * The Astrolib Day (JD) is calculated using UT and the Astrolib Ephemeris
     * Day (JDE) is calculated using TT. In the following steps, note that there
     * is a 10-day gap between the Astrolib and Gregorian calendar where the
     * Astrolib calendar ends on October 4, 1582 (JD = 2299160), and after
     * 10-days the Gregorian calendar starts on October 15, 1582
     *
     * @param c is input time to be converted julian day
     * @return julianDay julian day ( Julian date independent of time zone by
     * the way)
     */
    fun calculateJulianDay(c: GregorianCalendar): Double {
        var year = c[Calendar.YEAR]
        var month = c[Calendar.MONTH] + 1
        val day = c[Calendar.DAY_OF_MONTH]
        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]
        val second = c[Calendar.SECOND]
        // double tz=(c.getTimeZone().getRawOffset()
        // +c.getTimeZone().getDSTSavings())/ (60 * 60 * 1000);
        val tz = (c.timeZone.getOffset(c.timeInMillis) / 3600000).toDouble()
        val dayDecimal = day + (hour - tz + (minute + second / 60.0) / 60.0) / 24.0
        if (month < 3) {
            month += 12
            year--
        }
        var julianDay = (floor(365.25 * (year + 4716.0))
                + floor(30.6001 * (month + 1)) + dayDecimal) - 1524.5
        if (julianDay > 2299160.0) {
            val a = floor((year / 100).toDouble())
            julianDay += 2 - a + floor(a / 4)
        }
        return julianDay
    }

    /**
     * Calculate the Astrolib century (JC),
     *
     * JC =(jd-2451545.0)/36525.0
     *
     *
     * @param jd is the Astrolib Day
     * @return the Astrolib century (JC)
     */
    fun getJulianCentury(jd: Double) = (jd - 2451545.0) / 36525.0

    /**
     * Calculate the Astrolib Ephemeris Day (JDE),
     *
     * JDE = JD + ΔT/86400
     *
     *
     * @param jd is the Astrolib Day
     * @param ΔT is the difference between the Earth rotation time and the
     * Terrestrial Time (TT). ΔT = TT-UT .
     * @return JDE the Astrolib Ephemeris Day (JDE)
     */
    fun getJulianEphemerisDay(jd: Double, ΔT: Double) = jd + ΔT / 86400.0

    /**
     * Calculate the Astrolib Ephemeris Millennium (JME) for the 2000 standard
     * epoch,
     *
     * jme = jce/ 10
     *
     *
     * @param jde is the Astrolib Day
     * @return jme the the Astrolib Ephemeris Millennium (JME)
     */
    fun getJulianEphemerisCentury(jde: Double) = (jde - 2451545.0) / 36525.0

    /**
     * Calculate the Astrolib Ephemeris Millennium (JME) for the 2000 standard
     * epoch.
     *
     * jme =jce/10
     *
     *
     * @param jce is the Astrolib Day
     * @return jme the the Astrolib Ephemeris Millennium (JME)
     */
    fun getJulianEphemerisMillennium(jce: Double) = jce / 10.0

    /**
     * Calculate orbital positions of the Sun and Moon required by eclipse
     * predictions, are calculated using Terrestrial Dynamical Time (TD) because
     * it is a uniform time scale. However, world time zones and daily life are
     * based on Universal Time[1] (UT). In order to convert eclipse predictions
     * from TD to UT, the difference between these two time scales must be
     * known. The parameter delta-T (ΔT) is the arithmetic difference, in
     * seconds, between the two as: <A HREF="https://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html">DELTA T
     * (ΔT)</A>.
     *
     * https://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html
     *
     *
     * @param jd is the Astrolib Day
     * @return ΔT = TD - UT POLYNOMIAL EXPRESSIONS FOR DELTA T (ΔT) seconds:
     */
    fun calculateTimeDifference(jd: Double): Double {
        val ymd = getYMDHMSfromJulian(jd.toInt().toDouble())
        // System.out.println("Year: "+ymd[0]);
        val y = ymd[0] + (ymd[1] - 0.5) / 12.0
        // System.out.println("Year: "+y);
        val t: Double
        val u: Double
        var ΔT = 0.0
        val yearRanges = listOf(
            500, 1600, 1700, 1800, 1860, 1900, 1920, 1941, 1961, 1986, 2005, 2050, 2150
        )
        var range = 0
        while (yearRanges[range] <= y) {
            range++
        }
        when (range) {
            1 -> {
                u = (y - 1000.0) / 100.0
                ΔT = (1574.2
                        + u
                        * (-556.01 + u
                        * (71.23472 + u
                        * (0.319781 + u
                        * (-0.8503463 + u
                        * (-0.005050998 + u * 0.0083572073))))))
            }
            2 -> {
                t = y - 1600
                ΔT = 120 + t * (-0.9808 + t * (-0.01532 + t * (1.0 / 7129.0)))
            }
            3 -> {
                t = y - 1700
                ΔT = (8.83
                        + t
                        * (0.1603 + t
                        * (-0.0059285 + t
                        * (0.00013336 + t * (-1.0 / 1174000.0)))))
            }
            4 -> {
                t = y - 1800
                ΔT = (13.72
                        + t
                        * (-0.332447 + t
                        * (0.0068612 + t
                        * (0.0041116 + t
                        * (-0.00037436 + t
                        * (0.0000121272 + t
                        * (-0.0000001699 + t * 0.000000000875)))))))
            }
            5 -> {
                t = y - 1860
                ΔT = (7.62
                        + t
                        * (0.5737 + t
                        * (-0.251754 + t
                        * (0.01680668 + t
                        * (-0.0004473624 + t
                        * (1.0 / 233174.0))))))
            }
            6 -> {
                t = y - 1900
                ΔT = (-2.79
                        + t
                        * (1.494119 + t
                        * (-0.0598939 + t * (0.0061966 + t * -0.000197))))
            }
            7 -> {
                t = y - 1920
                ΔT = 21.20 + t * (0.84493 + t * (-0.076100 + t * 0.0020936))
            }
            8 -> {
                t = y - 1950
                ΔT = 29.07 + t * (0.407 + t * (-1.0 / 233.0 + t * (1.0 / 2547.0)))
            }
            9 -> {
                t = y - 1975
                ΔT = 45.45 + t * (1.067 + t * (-1 / 260.0 + t * (-1.0 / 718.0)))
            }
            10 -> {
                t = y - 2000
                ΔT = (63.86
                        + t
                        * (0.3345 + t
                        * (-0.060374 + t
                        * (0.0017275 + t
                        * (0.000651814 + t * 0.00002373599)))))
            }
            11 -> {
                t = y - 2000
                ΔT = 62.92 + t * (0.32217 + t * 0.005589)
            }
            12 -> ΔT = (-20 + 32 * ((y - 1820.0) / 100.0) * ((y - 1820.0) / 100.0)
                    - 0.5628 * (2150.0 - y))
        }
        return ΔT
    }

    /**
     * @param hour is the hour with double pressicion
     * @return int[] {year, month, day};
     */
    fun convertHour2HHMMSS(hour: Double): List<Int> {
        val Seconds = (hour * 3600).toInt() % 3600 % 60
        val Minute = (hour * 60).toInt() % 60
        val Hour = hour.toInt()
        return listOf(Hour, Minute, Seconds)
    }

    /**
     * @param hour is the hour with double pressicion
     * @return int[] {Hour, Minute};;
     */
    fun convertHour2HHMM(hour: Double): List<Int> {
        val Minute = (hour * 60).roundToLong().toInt() % 60
        val Hour = hour.toInt()
        return listOf(Hour, Minute)
    }

    /**
     * @param hour is the hour with double pressicion
     * @return HH MM SS in String
     */
    fun getStringHHMMSSS(hour: Double): String {
        val HHMMSS = convertHour2HHMMSS(hour)
        return "${intTwoDigit(HHMMSS[0])}:${intTwoDigit(HHMMSS[1])}:${intTwoDigit(HHMMSS[2])}"
    }

    /**
     * @param hour is the hour with double pressicion
     * @return HH MM in String
     */
    fun getStringHHMM(hour: Double): String {
        val HHMM = convertHour2HHMM(hour)
        return "${intTwoDigit(HHMM[0])}:${intTwoDigit(HHMM[1])}"
    }

    fun intTwoDigit(i: Int): String {
        return (if (i < 10) "0" else "") + i
    }

    /**
     * Converts a Astrolib day to a calendar date ref: Numerical Recipes in C,
     * 2nd ed., Cambridge University Press 1992
     *
     * @param julianDay is the Astrolib Day
     * @return int[] {year, month, day};
     */
    fun fromJulian(julianDay: Double): List<Int> {
        // this.julianDay = julianDay;
        val jd = julianDay + 0.5
        val z = floor(jd).toInt()
        val f = jd - z // a fraction between zero and one
        var a = z
        if (z >= 2299161) { // (typo in the book)
            val alpha = floor((z - 1867216.25) / 36524.25).toInt()
            a += 1 + alpha - floor(alpha / 4.0).toInt()
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toInt()
        val dd = floor(365.25 * c).toInt()
        val e = floor((b - dd) / 30.6001).toInt()
        // and then,
        // double d = b - dd - Math.floor(30.6001 * e) + f;
        val day = (b - dd - floor(30.6001 * e) + f).toInt()
        val month = e - if (e < 14) 1 else 13
        // sometimes the year is too high by one
        val year = c - if (month > 2) 4716 else 4715
        val Hour = 24.0 * (julianDay + 0.5 - (julianDay + 0.5).toInt())
        // int minute = (int) Math.round((Hour - (int) (Hour)) * 60.0);
        // Minute =((int)(hour*60))%60;
        val hour = Hour.toInt()
        val seconds = (Hour * 3600).toInt() % 3600 % 60
        val minute = (Hour * 60).toInt() % 60
        return listOf(year, month, day, hour, minute, seconds)
    }

    /**
     * Converts a Astrolib day to a calendar date ref: Numerical Recipes in C,
     * 2nd ed., Cambridge University Press 1992
     *
     * @param julianDay is the Astrolib Day
     * @return int[] {year, month, day,hour, minute,seconds} output always must
     * be in Universal Time UT;
     */
    fun getYMDHMSfromJulian(julianDay: Double): List<Int> {
        // this.julianDay = julianDay;
        val jd = julianDay + 0.5
        val z = floor(jd).toInt()
        val f = jd - z // a fraction between zero and one
        var a = z
        if (z >= 2299161) { // (typo in the book)
            val alpha = floor((z - 1867216.25) / 36524.25).toInt()
            a += 1 + alpha - floor(alpha / 4.0).toInt()
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toInt()
        val dd = floor(365.25 * c).toInt()
        val e = floor((b - dd) / 30.6001).toInt()
        // and then,
        // double d = b - dd - Math.floor(30.6001 * e) + f;
        val day = (b - dd - floor(30.6001 * e) + f).toInt()
        val month = e - if (e < 14) 1 else 13
        // sometimes the year is too high by one
        val year = c - if (month > 2) 4716 else 4715
        val Hour = 24.0 * (julianDay + 0.5 - (julianDay + 0.5).toInt())
        // int minute = (int) Math.round((Hour - (int) (Hour)) * 60.0);
        // Minute =((int)(hour*60))%60;
        val hour = Hour.toInt()
        val seconds = (Hour * 3600).toInt() % 3600 % 60
        val minute = (Hour * 60).toInt() % 60
        return listOf(year, month, day, hour, minute, seconds)
    }

    /**
     * Converts a Astrolib day to a calendar date ref: Numerical Recipes in C,
     * 2nd ed., Cambridge University Press 1992
     *
     * @param julianDay is the Astrolib Day
     * @return new GregorianCalendar (year, month, day, hour, minute,seconds);
     */
    fun convertJulian2Gregorian(julianDay: Double): GregorianCalendar {
        // this.julianDay = julianDay;
        val jd = julianDay + 0.5
        val z = floor(jd).toInt()
        val f = jd - z // a fraction between zero and one
        var a = z
        if (z >= 2299161) { // (typo in the book)
            val alpha = floor((z - 1867216.25) / 36524.25).toInt()
            a += 1 + alpha - floor(alpha / 4.0).toInt()
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toInt()
        val dd = floor(365.25 * c).toInt()
        val e = floor((b - dd) / 30.6001).toInt()
        // and then,
        // double d = b - dd - Math.floor(30.6001 * e) + f;
        val day = (b - dd - floor(30.6001 * e) + f).toInt()
        val month = e - if (e < 14) 1 else 13
        // sometimes the year is too high by one
        val year = c - if (month > 2) 4716 else 4715
        val Hour = 24.0 * (julianDay + 0.5 - (julianDay + 0.5).toInt())
        // int minute = (int) Math.round((Hour - (int) (Hour)) * 60.0);
        // Minute =((int)(hour*60))%60;
        val hour = Hour.toInt()
        val seconds = (Hour * 3600).toInt() % 3600 % 60
        val minute = (Hour * 60).toInt() % 60
        return GregorianCalendar(
            year, month - 1, day, hour, minute,
            seconds
        )
    }

    /**
     * Converts a julian day to a calendar date
     *
     * @param julianDay
     * @return String DD/MM/YYYY HH:MM:SS
     */
    fun fromJulianToCalendarStr(julianDay: Double): String {
        val julian = getYMDHMSfromJulian(julianDay)
        return ("  " + intTwoDigit(julian[2]) + "/" + intTwoDigit(julian[1])
                + "/" + julian[0] + " " + intTwoDigit(julian[3]) + ":"
                + intTwoDigit(julian[4]) + ":" + intTwoDigit(julian[5]))
    }

    fun getHHMMSSfromGreCal(gdate: GregorianCalendar): String {
        return (gdate[Calendar.HOUR_OF_DAY].toString() + ":"
                + intTwoDigit(gdate[Calendar.MINUTE]) + ":"
                + intTwoDigit(gdate[Calendar.SECOND]))
    }

    fun getDDMMHHMMSSfromGreCal(gdate: GregorianCalendar): String {
        return (gdate[Calendar.DAY_OF_MONTH].toString() + "/"
                + gdate[Calendar.MONTH] + " "
                + gdate[Calendar.HOUR_OF_DAY] + ":"
                + intTwoDigit(gdate[Calendar.MINUTE]) + ":"
                + intTwoDigit(gdate[Calendar.SECOND]))
    }

    /**
     * Converts a julian day to a calendar date
     *
     * @param julianDay
     * @return String DD/MM/YYYY
     */
    fun fromJulianToCalendarDateStr(julianDay: Double): String {
        val julian = getYMDHMSfromJulian(julianDay)
        return ("  " + intTwoDigit(julian[2]) + "/" + intTwoDigit(julian[1])
                + "/" + julian[0])
    }

    fun thirdOrderPolynomial(a: Double, b: Double, c: Double, d: Double, x: Double) =
        ((a * x + b) * x + c) * x + d

    fun fourthOrderPolynomial(a: Double, b: Double, c: Double, d: Double, e: Double, x: Double) =
        (((a * x + b) * x + c) * x + d) * x + e

    /**
     * Compute the Atmospheric Refraction in minutes at apparent angle in
     * degrees, R=1/(tan(ho+7.31/(ho+4.4)))+0.001351521723799; R is the
     * atmospheric refraction at the apparent altitude h0 degrees;
     *
     * @param ho ho is the apparent altitude for celestial object in degrees.
     * @result correction for altitude angle in Minutes
     */
    fun getApparentAtmosphericRefraction(ho: Double): Double {
        val R = 1 / tan(Math.toRadians(ho + 7.31 / (ho + 4.4))) + 0.001351521723799
        return R / 60
    }

    /**
     * Compute the Atmospheric Refraction in minutes at angle in degrees,
     * R=1.02/(tan(h+10.3/(h+5.11)))++0.0019279; R is the atmospheric refraction
     * at the apparent altitude h0 degrees;
     *
     * @param h ho is the apparent altitude for celestial object in degrees.
     * @result correction for altitude angle in Minutes
     */
    fun getAtmosphericRefraction(h: Double): Double {
        val R = (1.02 / tan(Math.toRadians(h + 10.3 / (h + 5.11)))
                + +0.0019279)
        return R / 60
    }

    /**
     * Computes the refraction correction angle. The effects of the atmosphere
     * vary with atmospheric pressure, humidity and other variables. Therefore
     * the calculation is approximate. Errors can be expected to increase the
     * further away you are from the equator, because the sun rises and sets at
     * a very shallow angle. Small variations in the atmosphere can have a
     * larger effect.
     *
     * @param zenith The sun zenith angle in degrees.
     * @return The refraction correction in degrees.
     */
    fun refractionCorrection(zenith: Double): Double {
        val exoatmElevation = 90 - zenith
        if (exoatmElevation > 85) {
            return 0.0
        }
        val te = tan(Math.toRadians(exoatmElevation))
        val refractionCorrection = if (exoatmElevation > 5.0) { // In minute of degrees
            58.1 / te - 0.07 / (te * te * te) + 0.000086 / te * te * te * te * te
        } else {
            if (exoatmElevation > -0.575) {
                (1735.0
                        + exoatmElevation
                        * (-518.2 + exoatmElevation
                        * (103.4 + exoatmElevation
                        * (-12.79 + exoatmElevation * 0.711))))
            } else -20.774 / te
        }
        return refractionCorrection / 3600
    }

    /**
     * Compute the Weather Correction Coefficent for given Temperature and
     * Pressure, P 283.15 k=----- ------ 1010 273.15+T k is the Correction
     * Coefficent for athmospheric refraction which is R*k;
     *
     * @param T Temperature in Celcilus
     * @param P Pressure in milliBars
     * @result Weather Correction Coefficent unitless
     */
    fun getWeatherCorrectionCoefficent(T: Int, P: Int) = P * 283.15 / (1010.0 * (273.15 + T))

    /**
     * Compute the elevation correction for sunset an sunrise,
     *
     * @param h altitude in meter from sea level
     * @result corection for altitude angle in degrees
     */
    fun getAltitudeCorrection(h: Int) = 0.0347 * sqrt(h.toDouble())

    fun limitDegrees(degrees: Double): Double {
        var degrees = degrees
        degrees /= 360.0
        var limited = 360.0 * (degrees - floor(degrees))
        if (limited < 0) {
            limited += 360.0
        }
        return limited
    }

    // ------------------------------------------------------------------------------
    //
    // Pegasus: Root finder using the Pegasus method
    //
    // Input:
    //
    // PegasusFunct Pointer to the function to be examined
    //
    // LowerBound Lower bound of search interval
    // UpperBound Upper bound of search interval
    // Accuracy Desired accuracy for the root
    //
    // Output:
    //
    // Root Root found (valid only if Success is true)
    // Success Flag indicating success of the routine
    //
    // References:
    //
    // Dowell M., Jarratt P., 'A modified Regula Falsi Method for Computing
    // the root of an equation', BIT 11, p.168-174 (1971).
    // Dowell M., Jarratt P., 'The "PEGASUS Method for Computing the root
    // of an equation', BIT 12, p.503-508 (1972).
    // G.Engeln-Muellges, F.Reutter, 'Formelsammlung zur Numerischen
    // Mathematik mit FORTRAN77-Programmen', Bibliogr. Institut,
    // Zuerich (1986).
    //
    // Notes:
    //
    // Pegasus assumes that the root to be found is bracketed in the interval
    // [LowerBound, UpperBound]. Ordinates for these abscissae must therefore
    // have different signs.
    //
    // ------------------------------------------------------------------------------
    fun Pegasus(
        LowerBound: Double, UpperBound: Double,
        ΔT: Double, Accuracy: Double, Success: BooleanArray,
        phase: Int
    ): Double {
        var x1 = LowerBound
        var x2 = UpperBound
        var f1 = searchPhaseEvent(x1, ΔT, phase)
        var f2 = searchPhaseEvent(x2, ΔT, phase)
        val MaxIterat = 30
        var Iterat = 0
        // Initialization
        Success[0] = false
        var Root = x1
        // Iteration
        if (f1 * f2 < 0.0) {
            do {
                // Approximation of the root by interpolation
                val x3 = x2 - f2 / ((f2 - f1) / (x2 - x1))
                val f3 = searchPhaseEvent(x3, ΔT, phase)

                // Replace (x1,f2) and (x2,f2) by new values, such that
                // the root is again within the interval [x1,x2]
                if (f3 * f2 <= 0.0) {
                    // Root in [x2,x3]
                    x1 = x2
                    f1 = f2 // Replace (x1,f1) by (x2,f2)
                    x2 = x3
                    f2 = f3 // Replace (x2,f2) by (x3,f3)
                } else {
                    // Root in [x1,x3]
                    f1 = f1 * f2 / (f2 + f3) // Replace (x1,f1) by (x1,f1')
                    x2 = x3
                    f2 = f3 // Replace (x2,f2) by (x3,f3)
                }
                Root = if (abs(f1) < abs(f2)) {
                    x1
                } else {
                    x2
                }
                Success[0] = abs(x2 - x1) <= Accuracy
                Iterat++
            } while (!Success[0] && Iterat < MaxIterat)
        }
        return Root
    }
}
