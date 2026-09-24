package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.getDateInstance
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import kotlin.test.Test
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

    @Test
    fun `test World Philosophy Day instances`() {
        listOf(
            Triple(2024, 11, 21),
            Triple(2025, 11, 20),
            Triple(2026, 11, 19),
            Triple(2027, 11, 18),
            Triple(2028, 11, 16),
            Triple(2029, 11, 15),
            Triple(2030, 11, 21),
            Triple(2031, 11, 20),
            Triple(2032, 11, 18),
            Triple(2033, 11, 17),
        ).forEach { (year, month, day) ->
            val calendar = Calendar.GREGORIAN
            assertEquals(day, calendar.getNthWeekDayOfMonth(year, month, 5, 3))
            val title = "روز جهانی فلسفه (سومین پنجشنبهٔ نوامبر)"
            val event = mapOf<String, Any>(
                "rule" to "nth weekday of month", "nth" to 3, "weekday" to 5, "month" to 11,
                "type" to "International", "title" to title, "holiday" to "false",
            )
            assertEquals(getDateInstance(event, year, calendar), CivilDate(year, month, day))
        }
    }

    @Test
    fun `getNthWeekDayOfMonth calculations correctness`() {
        listOf(
            listOf(1400, 12, 15, 1, 3),
            listOf(1400, 12, 21, 7, 3),
            listOf(1400, 12, 20, 6, 3),
            listOf(1400, 12, 19, 5, 3),
            listOf(1400, 12, 18, 4, 3),
            listOf(1400, 12, 17, 3, 3),
            listOf(1400, 12, 16, 2, 3),
            listOf(1400, 12, 20, 6, 3),

            listOf(1400, 12, 7, 7, 1),
            listOf(1400, 12, 14, 7, 2),
            listOf(1400, 12, 21, 7, 3),
            listOf(1400, 12, 28, 7, 4),

            listOf(1400, 12, 1, 1, 1),
            listOf(1400, 12, 8, 1, 2),
            listOf(1400, 12, 15, 1, 3),
            listOf(1400, 12, 22, 1, 4),
            listOf(1400, 12, 29, 1, 5),

            listOf(1400, 12, 2, 2, 1),
            listOf(1400, 12, 9, 2, 2),
            listOf(1400, 12, 16, 2, 3),
            listOf(1400, 12, 23, 2, 4),

            listOf(1400, 12, 3, 3, 1),
            listOf(1400, 12, 10, 3, 2),
            listOf(1400, 12, 17, 3, 3),
            listOf(1400, 12, 24, 3, 4),

            listOf(1400, 12, 4, 4, 1),
            listOf(1400, 12, 11, 4, 2),
            listOf(1400, 12, 18, 4, 3),
            listOf(1400, 12, 25, 4, 4),

            listOf(1400, 12, 5, 5, 1),
            listOf(1400, 12, 12, 5, 2),
            listOf(1400, 12, 19, 5, 3),
            listOf(1400, 12, 26, 5, 4),

            listOf(1400, 12, 6, 6, 1),
            listOf(1400, 12, 13, 6, 2),
            listOf(1400, 12, 20, 6, 3),
            listOf(1400, 12, 27, 6, 4),
        ).forEach { (year, month, day, weekDay, nth) ->
            val calendar = Calendar.SHAMSI
            assertEquals(day, calendar.getNthWeekDayOfMonth(year, month, weekDay, nth))
            val event = mapOf<String, Any>(
                "rule" to "nth weekday of month", "nth" to nth,
                "weekday" to weekDay, "month" to month,
            )
            assertEquals(getDateInstance(event, year, calendar), PersianDate(year, month, day))
        }
    }

    @Test
    fun `getLastWeekDayOfMonth calculations correctness`() {
        listOf(
            listOf(1400, 12, 29, 1),
            listOf(1400, 12, 28, 7),
            listOf(1400, 12, 27, 6),
            listOf(1400, 12, 26, 5),
            listOf(1400, 12, 25, 4),
            listOf(1400, 12, 24, 3),
            listOf(1400, 12, 23, 2),
            listOf(1400, 12, 27, 6),
            listOf(1399, 12, 29, 6),
            listOf(1398, 12, 23, 6),
            listOf(1397, 12, 24, 6),
            listOf(1396, 12, 25, 6),
            listOf(1395, 12, 27, 6),
            listOf(1394, 12, 28, 6),
            listOf(1393, 12, 29, 6),
            listOf(1392, 12, 23, 6),
            listOf(1391, 12, 25, 6),
            listOf(1390, 12, 26, 6),
        ).forEach { (year, month, day, weekDay) ->
            val calendar = Calendar.SHAMSI
            assertEquals(day, calendar.getLastWeekDayOfMonth(year, month, weekDay))
            val event = mapOf(
                "rule" to "last weekday of month", "weekday" to weekDay, "month" to month,
            )
            assertEquals(getDateInstance(event, year, calendar), PersianDate(year, month, day))
        }
    }
}
