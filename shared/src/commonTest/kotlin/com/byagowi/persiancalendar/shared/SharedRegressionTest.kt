package com.byagowi.persiancalendar.shared

import com.byagowi.persiancalendar.entities.Numeral
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.illumination
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calculator.eval
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.PrayTimes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedRegressionTest {
    @Test fun magneticModelMatchesAndroidReference() {
        // Fixtures obtained by compiling Android 16's original GeomagneticField.java.
        listOf(
            listOf(35.6892, 51.389, 27948.006, 2369.0334, 39597.145),
            listOf(0.0, 0.0, 27536.402, -2248.5867, -16022.431),
            listOf(80.0, -120.0, 1850.6251, 184.83928, 57181.234),
        ).forEach { fixture ->
            val field = magneticField(fixture[0], fixture[1], 0.0, 1577836800000.0)
            assertEquals(fixture[2], field.north, .1)
            assertEquals(fixture[3], field.east, .1)
            assertEquals(fixture[4], field.down, .1)
        }
    }

    @Test fun calendarBoundaries() {
        assertEquals(CivilDate(2024, 3, 20).toJdn(), PersianDate(1403, 1, 1).toJdn())
        assertEquals(30, CalendarKind.PERSIAN.monthLength(1399, 12))
        assertEquals(29, CalendarKind.PERSIAN.monthLength(1400, 12))
        assertEquals(29, CalendarKind.GREGORIAN.monthLength(2024, 2))
        assertFails { CalendarKind.GREGORIAN.validDate(2023, 2, 29) }
        CalendarKind.entries.forEach { calendar ->
            for (day in CivilDate(2000, 1, 1).toJdn()..CivilDate(2030, 12, 31).toJdn() step 13) {
                assertEquals(day, calendar.date(day).toJdn(), "$calendar $day")
            }
        }
    }

    @Test fun numeralGrouping() {
        assertEquals("1,234,567", Numeral.ARABIC.formatLongNumber(1234567))
        assertEquals("-۹٬۲۲۳٬۳۷۲٬۰۳۶٬۸۵۴٬۷۷۵٬۸۰۸", Numeral.PERSIAN.formatLongNumber(Long.MIN_VALUE))
        assertEquals(123.5, Numeral.PERSIAN.parseDouble("۱۲۳٫۵"))
    }

    @Test fun calculatorGrammarParity() {
        mapOf("2 ^ 2 ^ 3" to "256", "2+2*2" to "6", "sin 90 deg" to "1", "cos sin 0" to "1", "a = cos; a(0)" to "1", "sin = cos; clear; sin 0" to "0", "(3,2,2)" to "(3, 2, 2)", "2 - 2 - 2" to "-2", "-2^2" to "4", "# comment\n2+3" to "5").forEach { (expression, result) -> assertEquals(result, eval(expression), expression) }
        assertTrue(eval("1d 2h 3m 4s + 4h 5s - 2030s + 28h").startsWith("2d 9h 29m 19s"))
        listOf("5+5 5 6+7", "(2+3", "2@3", "2+").forEach { assertFails(it) { eval(it) } }
    }

    @Test fun prayerTimesMatchExistingAndroidFixtures() {
        val times = PrayTimes(CalculationMethod.MWL, 2018, 9, 5, -4.0, Coordinates(43.0, -80.0, 0.0), AsrMethod.Standard, HighLatitudesMethod.NightMiddle)
        assertEquals("05:09", clockText(times.fajr))
        assertEquals("06:48", clockText(times.sunrise))
        assertEquals("13:18", clockText(times.dhuhr))
        assertEquals("16:57", clockText(times.asr))
        assertEquals("19:47", clockText(times.maghrib))
        assertEquals("21:21", clockText(times.isha))
        assertEquals("—", clockText(Double.NaN))
    }

    @Test fun astronomyTimeAndSeasons() {
        assertEquals("2000-01-01T12:00:00.000Z", astronomyTime(CivilDate(2000, 1, 1).toJdn()).toString())
        assertEquals(946728000000.0, astronomyTime(CivilDate(2000, 1, 1).toJdn()).epochMillis(), 1.0)
        val equinox = seasons(2024).marchEquinox.toDateTime()
        assertEquals(3, equinox.month)
        assertEquals(20, equinox.day)
        assertTrue(illumination(Body.Moon, Time(2024, 4, 8, 18, 0, 0.0)).phaseFraction < .001)
    }

    @Test fun irregularEventRules() {
        fun event(rule: String, fields: Map<String, String>) = BundledEvent("test", false, "International", CalendarKind.GREGORIAN, fields + ("rule" to rule))
        assertEquals(CivilDate(2024, 2, 29).toJdn(), event("end of month", mapOf("month" to "2")).dayInYear(2024))
        assertEquals(CivilDate(2024, 11, 28).toJdn(), event("nth weekday of month", mapOf("month" to "11", "weekday" to "5", "nth" to "4")).dayInYear(2024))
        assertNull(event("single event", mapOf("year" to "2023", "month" to "1", "day" to "1")).dayInYear(2024))
    }
}
