package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Clock
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.PrayTimes
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class PrayTimeTests {

    @Test
    fun `pray times calculations correctness`() {
        // http://praytimes.org/code/v2/js/examples/monthly.htm
        var prayTimes = PrayTimes(
            CalculationMethod.MWL,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )

        assertEquals(Clock(5, 9).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 21).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.ISNA,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 27).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 9).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Egypt,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Hanafi
        )
        assertEquals(Clock(5, 0).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(17, 53).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 24).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Makkah,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 6).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 18).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Karachi,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 9).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 27).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Jafari,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 21).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(20, 5).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 3).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Tehran,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 11).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(20, 8).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 3).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Tehran,
            createCalendar("GMT+8:00", 2019, 6, 9),
            Coordinates(3.147778, 101.695278, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 49).toMinutes(), Clock.fromDouble(prayTimes.fajr).toMinutes())
        assertEquals(Clock(7, 3).toMinutes(), Clock.fromDouble(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 12).toMinutes(), Clock.fromDouble(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 39).toMinutes(), Clock.fromDouble(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 37).toMinutes(), Clock.fromDouble(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(20, 19).toMinutes(), Clock.fromDouble(prayTimes.isha).toMinutes())
    }

    private fun createCalendar(timeZone: String, year: Int, month: Int, dayOfMonth: Int) =
        GregorianCalendar(TimeZone.getTimeZone(timeZone)).apply {
            set(year, month - 1, dayOfMonth, 0, 0)
        }
}
