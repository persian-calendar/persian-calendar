package com.byagowi.persiancalendar

import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class PrayTimeTests {
    @Test
    fun `pray times calculations correctness`() {
        fun getDate(year: Int, month: Int, dayOfMonth: Int): Date =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(year, month - 1, dayOfMonth, 0, 0)
            }.time

        // http://praytimes.org/code/v2/js/examples/monthly.htm
        var prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.MWL,
            getDate(2018, 9, 5),
            Coordinate(43.0, -80.0, 0.0),
            -5.0, true
        )

        assertEquals(Clock(5, 9).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 21).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.ISNA,
            getDate(2018, 9, 5),
            Coordinate(43.0, -80.0, 0.0),
            -5.0, true
        )
        assertEquals(Clock(5, 27).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 9).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.Egypt,
            getDate(2018, 9, 5),
            Coordinate(43.0, -80.0, 0.0),
            -5.0, true
        )
        assertEquals(Clock(5, 0).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 24).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.Makkah,
            getDate(2018, 9, 5),
            Coordinate(43.0, -80.0, 0.0),
            -5.0, true
        )
        assertEquals(Clock(5, 6).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 18).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.Karachi,
            getDate(2018, 9, 5),
            Coordinate(43.0, -80.0, 0.0),
            -5.0, true
        )
        assertEquals(Clock(5, 9).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 27).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.Jafari,
            getDate(2018, 9, 5),
            Coordinate(43.0, -80.0, 0.0),
            -5.0, true
        )
        assertEquals(Clock(5, 21).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(20, 5).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 3).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.Tehran,
            getDate(2018, 9, 5),
            Coordinate(43.0, -80.0, 0.0),
            -5.0, true
        )
        assertEquals(Clock(5, 11).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(20, 8).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 3).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(
            CalculationMethod.Tehran,
            getDate(2019, 6, 9),
            Coordinate(3.147778, 101.695278, 0.0),
            8.0, false
        )
        assertEquals(Clock(5, 49).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(7, 3).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 12).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 39).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 37).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(20, 19).toInt(), prayTimes.ishaClock.toInt())
    }
}
