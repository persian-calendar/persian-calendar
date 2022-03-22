package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.EventsRepository
import com.byagowi.persiancalendar.utils.EventsStore
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EventsTests {

    @Test
    fun `Test Iran default events repository`() {
        val repository = EventsRepository(EventsRepository.iranDefault, Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, false)

        (1..30).map { IslamicDate(1400, 2, it) }.flatMap {
            repository.irregularCalendarEventsStore
                .getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.let {
            assertEquals(1, it.size)
            assertEquals(true, it[0].isHoliday)
        }

        repository.irregularCalendarEventsStore.getEventsList<CalendarEvent.PersianCalendarEvent>(
            1400, CalendarType.SHAMSI
        ).let { assertEquals(0, it.size) }
    }

    @Test
    fun `Test ancient Iran events repository`() {
        val repository = EventsRepository(setOf(EventsRepository.iranAncientKey), Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, false)

        (1..30).map { IslamicDate(1400, 2, it) }.flatMap {
            repository.irregularCalendarEventsStore
                .getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.let { assertEquals(0, it.size) }

        repository.irregularCalendarEventsStore.getEventsList<CalendarEvent.PersianCalendarEvent>(
            1400, CalendarType.SHAMSI
        ).let { assertEquals(1, it.size) }

        assertEquals(
            1, repository.getEvents(Jdn(PersianDate(1400, 12, 24)), EventsStore.empty()).size
        )
        assertEquals(
            1, repository.getEvents(Jdn(PersianDate(1400, 12, 1)), EventsStore.empty()).size
        )
    }

    @Test
    fun `Test Internation events repository`() {
        val repository = EventsRepository(setOf(EventsRepository.internationalKey), Language.UR)
        assertEquals(IslamicDate.useUmmAlQura, true)
        repository.irregularCalendarEventsStore
            .getEventsList<CalendarEvent.GregorianCalendarEvent>(2021, CalendarType.GREGORIAN)
            .let {
                assertEquals(2, it.size)
                assertEquals(false, it[0].isHoliday)
                assertEquals(false, it[1].isHoliday)
            }
    }

    @Test
    fun `Test Afghanistan default repository`() {
        val repository = EventsRepository(EventsRepository.afghanistanDefault, Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, true)

        (1..31).map { IslamicDate(1400, 2, it) }.flatMap {
            repository.irregularCalendarEventsStore.getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.let { assertEquals(0, it.size) }
    }

    @Test
    fun `Test whether events repository sets IslamicDate global variable correctly`() {
        run {
            EventsRepository(setOf(), Language.FA_AF)
            assertEquals(IslamicDate.useUmmAlQura, true)
        }
        run {
            EventsRepository(setOf(), Language.PS)
            assertEquals(IslamicDate.useUmmAlQura, true)
        }
        run {
            EventsRepository(setOf(), Language.FA)
            assertEquals(IslamicDate.useUmmAlQura, false)
        }
    }

    @Test
    fun `EventsStore hash uniqueness for days of year`() {
        (1..12).flatMap { month -> (1..31).map { day -> CivilDate(2000, month, day) } }
            .map { EventsStore.hash(it) }.toSet().toList()
            .also { assertEquals(372, it.size) }
    }
}
