package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Clock
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.PrayTimes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

        assertEquals(Clock(5, 9).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 21).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.ISNA,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 27).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 9).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Egypt,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Hanafi
        )
        assertEquals(Clock(5, 0).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(17, 53).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 24).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Makkah,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 6).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 18).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Karachi,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 9).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 48).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 27).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Jafari,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 21).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(20, 5).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 3).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Tehran,
            createCalendar("GMT-4:00", 2018, 9, 5),
            Coordinates(43.0, -80.0, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 11).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(6, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(20, 8).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(21, 3).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = PrayTimes(
            CalculationMethod.Tehran,
            createCalendar("GMT+8:00", 2019, 6, 9),
            Coordinates(3.147778, 101.695278, 0.0),
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(Clock(7, 3).toMinutes(), Clock.fromHoursFraction(prayTimes.sunrise).toMinutes())
        assertEquals(Clock(13, 12).toMinutes(), Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes())
        assertEquals(Clock(16, 39).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(Clock(19, 37).toMinutes(), Clock.fromHoursFraction(prayTimes.maghrib).toMinutes())
        assertEquals(Clock(20, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())
    }

    private fun createCalendar(timeZone: String, year: Int, month: Int, dayOfMonth: Int) =
        GregorianCalendar(TimeZone.getTimeZone(timeZone)).apply {
            set(year, month - 1, dayOfMonth, 0, 0)
        }
}
