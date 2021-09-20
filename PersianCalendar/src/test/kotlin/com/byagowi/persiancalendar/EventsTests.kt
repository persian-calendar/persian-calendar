package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.EnabledHolidays
import com.byagowi.persiancalendar.utils.EventsStore
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.irregularCalendarEventsStore
import com.byagowi.persiancalendar.utils.loadEvents
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.Assert.assertEquals
import org.junit.Test

class EventsTests {

    @Test
    fun `holidays load correctness`() {
        loadEvents(EnabledHolidays(EnabledHolidays.iranDefault), Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, false)

        (1..30).map { IslamicDate(1400, 2, it) }.flatMap {
            irregularCalendarEventsStore.getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.let {
            assertEquals(1, it.size)
            assertEquals(true, it[0].isHoliday)
        }

        irregularCalendarEventsStore.getEventsList<CalendarEvent.PersianCalendarEvent>(
            1400, CalendarType.SHAMSI
        ).let { assertEquals(0, it.size) }

        //
        loadEvents(EnabledHolidays(setOf(EnabledHolidays.iranAncientKey)), Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, false)

        (1..30).map { IslamicDate(1400, 2, it) }.flatMap {
            irregularCalendarEventsStore.getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.let { assertEquals(0, it.size) }

        irregularCalendarEventsStore.getEventsList<CalendarEvent.PersianCalendarEvent>(
            1400, CalendarType.SHAMSI
        ).let { assertEquals(1, it.size) }

        assertEquals(1, getEvents(Jdn(PersianDate(1400, 12, 24)), EventsStore.empty()).size)
        assertEquals(1, getEvents(Jdn(PersianDate(1400, 12, 1)), EventsStore.empty()).size)

        //
        loadEvents(EnabledHolidays(setOf(EnabledHolidays.internationalKey)), Language.UR)
        assertEquals(IslamicDate.useUmmAlQura, true)
        irregularCalendarEventsStore.getEventsList<CalendarEvent.GregorianCalendarEvent>(
            2021, CalendarType.GREGORIAN
        ).let {
            assertEquals(1, it.size)
            assertEquals(false, it[0].isHoliday)
        }

        //
        loadEvents(EnabledHolidays(EnabledHolidays.afghanistanDefault), Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, true)

        (1..31).map { IslamicDate(1400, 2, it) }.flatMap {
            irregularCalendarEventsStore.getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.let { assertEquals(0, it.size) }

        //
        loadEvents(EnabledHolidays(), Language.FA_AF)
        assertEquals(IslamicDate.useUmmAlQura, true)

        //
        loadEvents(EnabledHolidays(), Language.PS)
        assertEquals(IslamicDate.useUmmAlQura, true)

        //
        loadEvents(EnabledHolidays(), Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, false)
    }

    @Test
    fun `EventsStore hash uniqueness for days of year`() {
        (1..12).flatMap { month -> (1..31).map { day -> CivilDate(2000, month, day) } }
            .map { EventsStore.hash(it) }.toSet().toList()
            .also { assertEquals(372, it.size) }
    }
}
