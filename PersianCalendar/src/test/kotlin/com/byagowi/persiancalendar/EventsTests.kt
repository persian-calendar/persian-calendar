package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.getDateInstance
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import org.junit.jupiter.api.Test
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
    fun `test createDateFromSpecification`() {
        val title = "روز جهانی فلسفه (سومین پنج‌شنبهٔ نوامبر)"
        val event = mapOf(
            "rule" to "nth weekday of month", "nth" to "3", "weekday" to "6", "month" to "11",
            "type" to "International", "title" to title, "holiday" to "false",
        )
        assertEquals(getDateInstance(event, 2024, CalendarType.GREGORIAN), CivilDate(2024, 11, 21))
        assertEquals(getDateInstance(event, 2025, CalendarType.GREGORIAN), CivilDate(2025, 11, 20))
        assertEquals(getDateInstance(event, 2026, CalendarType.GREGORIAN), CivilDate(2026, 11, 19))
        assertEquals(getDateInstance(event, 2027, CalendarType.GREGORIAN), CivilDate(2027, 11, 18))
        assertEquals(getDateInstance(event, 2028, CalendarType.GREGORIAN), CivilDate(2028, 11, 16))
        assertEquals(getDateInstance(event, 2029, CalendarType.GREGORIAN), CivilDate(2029, 11, 15))
        assertEquals(getDateInstance(event, 2030, CalendarType.GREGORIAN), CivilDate(2030, 11, 21))
        assertEquals(getDateInstance(event, 2031, CalendarType.GREGORIAN), CivilDate(2031, 11, 20))
        assertEquals(getDateInstance(event, 2032, CalendarType.GREGORIAN), CivilDate(2032, 11, 18))
        assertEquals(getDateInstance(event, 2033, CalendarType.GREGORIAN), CivilDate(2033, 11, 17))
    }
}
