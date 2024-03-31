package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.getDateInstance
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class EventsTests {
    @Test
    fun `Test whether events repository sets IslamicDate global variable correctly`() {
        run {
            EventsRepository(emptySet(), Language.FA_AF)
            assertEquals(IslamicDate.useUmmAlQura, true)
        }
        run {
            EventsRepository(emptySet(), Language.PS)
            assertEquals(IslamicDate.useUmmAlQura, true)
        }
        run {
            EventsRepository(emptySet(), Language.FA)
            assertEquals(IslamicDate.useUmmAlQura, false)
        }
    }

    @Test
    fun `EventsStore hash uniqueness for days of year`() {
        (1..12).flatMap { month -> (1..31).map { day -> CivilDate(2000, month, day) } }
            .map { EventsStore.hash(it) }.toSet().toList()
            .also { assertEquals(372, it.size) }
    }

    @ParameterizedTest
    @CsvSource(
        "2024, 11, 21",
        "2025, 11, 20",
        "2026, 11, 19",
        "2027, 11, 18",
        "2028, 11, 16",
        "2029, 11, 15",
        "2030, 11, 21",
        "2031, 11, 20",
        "2032, 11, 18",
        "2033, 11, 17",
    )
    fun `test World Philosophy Day instances`(year: Int, month: Int, day: Int) {
        val calendar = CalendarType.GREGORIAN
        assertEquals(day, calendar.getNthWeekDayOfMonth(year, month, 6, 3))
        val title = "روز جهانی فلسفه (سومین پنج‌شنبهٔ نوامبر)"
        val event = mapOf(
            "rule" to "nth weekday of month", "nth" to "3", "weekday" to "6", "month" to "11",
            "type" to "International", "title" to title, "holiday" to "false",
        )
        assertEquals(getDateInstance(event, year, calendar), CivilDate(year, month, day))
    }

    @ParameterizedTest
    @CsvSource(
        "1400, 12, 29, 2",
        "1400, 12, 28, 1",
        "1400, 12, 27, 7",
        "1400, 12, 26, 6",
        "1400, 12, 25, 5",
        "1400, 12, 24, 4",
        "1400, 12, 23, 3",
        "1400, 12, 27, 7",
        "1399, 12, 29, 7",
        "1398, 12, 23, 7",
        "1397, 12, 24, 7",
        "1396, 12, 25, 7",
        "1395, 12, 27, 7",
        "1394, 12, 28, 7",
        "1393, 12, 29, 7",
        "1392, 12, 23, 7",
        "1391, 12, 25, 7",
        "1390, 12, 26, 7",
    )
    fun `getLastDayOfWeekOfMonth calculations correctness`(
        year: Int, month: Int, day: Int, weekDay: Int
    ) {
        val calendar = CalendarType.SHAMSI
        assertEquals(day, calendar.getLastWeekDayOfMonth(year, month, weekDay))
        val event = mapOf(
            "rule" to "last weekday of month", "weekday" to "$weekDay", "month" to "$month",
        )
        assertEquals(getDateInstance(event, year, calendar), PersianDate(year, month, day))
    }
}
