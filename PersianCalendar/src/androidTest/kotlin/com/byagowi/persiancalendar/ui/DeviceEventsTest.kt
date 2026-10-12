package com.byagowi.persiancalendar.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.DeviceEventInterval
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.GregorianCalendarEventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import io.github.persiancalendar.calendar.CivilDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class DeviceEventsTest {
    @Test
    fun deviceOccurrencesAndAnnualEventsRemainSeparate() {
        val repository = EventsRepository(emptySet(), Language.FA)
        val utc = TimeZone.getTimeZone("UTC")
        val occurrence = CalendarEvent.DeviceCalendarEvent(
            title = "Occurrence", isHoliday = false, source = null, id = 42, description = "",
            interval = DeviceEventInterval(
                DeviceEventInterval.startOfDay(CivilDate(2026, 10, 12), utc).timeInMillis,
                DeviceEventInterval.startOfDay(CivilDate(2026, 10, 15), utc).timeInMillis,
                true, TimeZone.getTimeZone("America/Los_Angeles"),
            ),
            color = "", time = null,
        )
        val deviceEvents = DeviceCalendarEventsStore(listOf(occurrence))
        (12..14).forEach { day ->
            assertSame(occurrence, repository.getEvents(Jdn(CivilDate(2026, 10, day)), deviceEvents).single())
        }
        assertTrue(repository.getEvents(Jdn(CivilDate(2026, 10, 15)), deviceEvents).isEmpty())
        val annual = CalendarEvent.GregorianCalendarEvent(
            "Annual", false, CivilDate(-1, 10, 12), null, emptyMap(),
        )
        val builtIns = GregorianCalendarEventsStore(listOf(annual))
        listOf(2026, 2027).forEach { year ->
            val events = builtIns.getEvents(
                CivilDate(year, 10, 12), repository.irregularCalendarEventsStore, deviceEvents,
            )
            assertTrue(events.any { it === annual })
            assertEquals(if (year == 2026) 1 else 0, events.count { it is CalendarEvent.DeviceCalendarEvent })
        }
    }
}
