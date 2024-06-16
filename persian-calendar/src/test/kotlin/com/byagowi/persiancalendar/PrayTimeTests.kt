package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.test.assertEquals

class PrayTimeTests {
    @ParameterizedTest
    @EnumSource(CalculationMethod::class)
    fun `smoke test different calculation methods`(method: CalculationMethod) {
        Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            method,
            AsrMethod.Standard
        )
    }

    @Test
    fun `pray times calculations correctness`() {
        // http://praytimes.org/code/v2/js/examples/monthly.htm
        var prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.MWL,
            AsrMethod.Standard
        )

        assertEquals(Clock(5, 9).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(6, 49).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 19).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(19, 48).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(21, 21).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.ISNA,
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 27).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(6, 49).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 19).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(19, 48).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(21, 9).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Egypt,
            AsrMethod.Hanafi
        )
        assertEquals(Clock(5, 0).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(6, 49).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 19).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(17, 53).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(19, 48).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(21, 24).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Makkah,
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 6).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(6, 49).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 19).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(19, 48).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(21, 18).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Karachi,
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 9).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(6, 49).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 19).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(19, 48).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(21, 27).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Jafari,
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 21).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(6, 49).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 19).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(20, 5).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(21, 3).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Tehran,
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 11).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(6, 49).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 19).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(16, 57).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(20, 8).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(21, 3).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())

        prayTimes = Coordinates(3.147778, 101.695278, 0.0).calculatePrayTimes(
            createCalendar("GMT+8:00", 2019, 6, 9),
            CalculationMethod.Tehran,
            AsrMethod.Standard
        )
        assertEquals(Clock(5, 49).toMinutes(), Clock.fromHoursFraction(prayTimes.fajr).toMinutes())
        assertEquals(
            Clock(7, 3).toMinutes(),
            Clock.fromHoursFraction(prayTimes.sunrise).toMinutes()
        )
        assertEquals(
            Clock(13, 12).toMinutes(),
            Clock.fromHoursFraction(prayTimes.dhuhr).toMinutes()
        )
        assertEquals(Clock(16, 39).toMinutes(), Clock.fromHoursFraction(prayTimes.asr).toMinutes())
        assertEquals(
            Clock(19, 37).toMinutes(),
            Clock.fromHoursFraction(prayTimes.maghrib).toMinutes()
        )
        assertEquals(Clock(20, 19).toMinutes(), Clock.fromHoursFraction(prayTimes.isha).toMinutes())
    }

    private fun createCalendar(
        timeZone: String, year: Int, month: Int, dayOfMonth: Int
    ): GregorianCalendar {
        return GregorianCalendar(TimeZone.getTimeZone(timeZone)).apply {
            set(year, month - 1, dayOfMonth, 0, 0)
        }
    }
}
