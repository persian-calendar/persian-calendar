package com.byagowi.persiancalendar.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.weekEnds
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventsTest {
    @Test
    fun testIranDefaultEventsRepository() {
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
    fun testAncientIranEventsRepository() {
        val repository = EventsRepository(setOf(EventsRepository.iranAncientKey), Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, false)

        assertEquals(0, (1..30).map { IslamicDate(1400, 2, it) }.flatMap {
            repository.irregularCalendarEventsStore
                .getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.size)

        assertEquals(
            1,
            repository.irregularCalendarEventsStore.getEventsList<CalendarEvent.PersianCalendarEvent>(
                1400, CalendarType.SHAMSI
            ).size
        )

        assertEquals(
            1, repository.getEvents(Jdn(PersianDate(1400, 12, 24)), EventsStore.empty()).size
        )
        assertEquals(
            1, repository.getEvents(Jdn(PersianDate(1400, 12, 14)), EventsStore.empty()).size
        )
    }

    @Test
    fun testCalculateWorkDays() {
        val weekEndsCopy = weekEnds.copyOf()
        weekEnds.indices.forEach { weekEnds[it] = false }
        Language.FA.defaultWeekEnds.mapNotNull(String::toIntOrNull).forEach { weekEnds[it] = true }
        val repository = EventsRepository(EventsRepository.iranDefault, Language.FA)
        assertEquals(
            35,
            repository.calculateWorkDays(
                Jdn(PersianDate(1402, 6, 10)),
                Jdn(PersianDate(1402, 7, 25))
            )
        )
        weekEndsCopy.forEachIndexed { index, b -> weekEnds[index] = b }
    }

    @Test
    fun testInternationEventsRepository() {
        val repository = EventsRepository(setOf(EventsRepository.internationalKey), Language.UR)
        assertEquals(IslamicDate.useUmmAlQura, true)
        repository.irregularCalendarEventsStore
            .getEventsList<CalendarEvent.GregorianCalendarEvent>(2021, CalendarType.GREGORIAN)
            .let {
                assertEquals(4, it.size)
                assertEquals(false, it[0].isHoliday)
                assertEquals(false, it[1].isHoliday)
            }
    }

    @Test
    fun testAfghanistanDefaultRepository() {
        val repository = EventsRepository(EventsRepository.afghanistanDefault, Language.FA)
        assertEquals(IslamicDate.useUmmAlQura, true)

        (1..31).map { IslamicDate(1400, 2, it) }.flatMap {
            repository.irregularCalendarEventsStore.getEvents<CalendarEvent.IslamicCalendarEvent>(it)
        }.let { assertEquals(0, it.size) }
    }
}
