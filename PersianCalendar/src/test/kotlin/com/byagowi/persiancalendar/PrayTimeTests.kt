package com.byagowi.persiancalendar

import androidx.collection.IntIntPair
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

        assertEquals(IntIntPair(5, 9), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(6, 49), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 19), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(16, 57), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(19, 48), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(21, 21), Clock(prayTimes.isha).toHoursAndMinutesPair())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.ISNA,
            AsrMethod.Standard
        )
        assertEquals(IntIntPair(5, 27), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(6, 49), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 19), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(16, 57), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(19, 48), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(21, 9), Clock(prayTimes.isha).toHoursAndMinutesPair())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Egypt,
            AsrMethod.Hanafi
        )
        assertEquals(IntIntPair(5, 0), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(6, 49), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 19), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(17, 53), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(19, 48), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(21, 24), Clock(prayTimes.isha).toHoursAndMinutesPair())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Makkah,
            AsrMethod.Standard
        )
        assertEquals(IntIntPair(5, 6), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(6, 49), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 19), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(16, 57), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(19, 48), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(21, 18), Clock(prayTimes.isha).toHoursAndMinutesPair())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Karachi,
            AsrMethod.Standard
        )
        assertEquals(IntIntPair(5, 9), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(6, 49), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 19), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(16, 57), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(19, 48), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(21, 27), Clock(prayTimes.isha).toHoursAndMinutesPair())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Jafari,
            AsrMethod.Standard
        )
        assertEquals(IntIntPair(5, 21), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(6, 49), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 19), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(16, 57), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(20, 5), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(21, 3), Clock(prayTimes.isha).toHoursAndMinutesPair())

        prayTimes = Coordinates(43.0, -80.0, 0.0).calculatePrayTimes(
            createCalendar("GMT-4:00", 2018, 9, 5),
            CalculationMethod.Tehran,
            AsrMethod.Standard
        )
        assertEquals(IntIntPair(5, 11), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(6, 49), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 19), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(16, 57), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(20, 8), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(21, 3), Clock(prayTimes.isha).toHoursAndMinutesPair())

        prayTimes = Coordinates(3.147778, 101.695278, 0.0).calculatePrayTimes(
            createCalendar("GMT+8:00", 2019, 6, 9),
            CalculationMethod.Tehran,
            AsrMethod.Standard
        )
        assertEquals(IntIntPair(5, 49), Clock(prayTimes.fajr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(7, 3), Clock(prayTimes.sunrise).toHoursAndMinutesPair())
        assertEquals(IntIntPair(13, 12), Clock(prayTimes.dhuhr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(16, 39), Clock(prayTimes.asr).toHoursAndMinutesPair())
        assertEquals(IntIntPair(19, 37), Clock(prayTimes.maghrib).toHoursAndMinutesPair())
        assertEquals(IntIntPair(20, 19), Clock(prayTimes.isha).toHoursAndMinutesPair())
    }

    private fun createCalendar(
        timeZone: String, year: Int, month: Int, dayOfMonth: Int
    ): GregorianCalendar {
        return GregorianCalendar(TimeZone.getTimeZone(timeZone)).apply {
            set(year, month - 1, dayOfMonth, 0, 0)
        }
    }
}
