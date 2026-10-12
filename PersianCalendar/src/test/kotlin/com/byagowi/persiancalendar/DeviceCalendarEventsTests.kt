package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.DeviceEventInterval
import io.github.persiancalendar.calendar.CivilDate
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DeviceCalendarEventsTests {
    private val zones = listOf("America/Los_Angeles", "Asia/Tehran", "UTC")
    private fun millis(instant: String): Long = assertNotNull(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }.parse(instant),
    ).time

    private fun interval(begin: String, end: String, allDay: Boolean, zone: String) =
        DeviceEventInterval(millis(begin), millis(end), allDay, TimeZone.getTimeZone(zone))

    private fun event(interval: DeviceEventInterval, id: Long = 42) = CalendarEvent.DeviceCalendarEvent(
        title = "Occurrence", isHoliday = false, source = null, id = id, description = "",
        interval = interval, color = "", time = if (interval.isAllDay) null else "23:00 – 01:00",
    )

    private fun localMillis(date: CivilDate, zone: String, hour: Int = 0, minute: Int = 0): Long =
        DeviceEventInterval.startOfDay(date, TimeZone.getTimeZone(zone)).apply {
            set(GregorianCalendar.HOUR_OF_DAY, hour)
            set(GregorianCalendar.MINUTE, minute)
        }.timeInMillis

    @Test
    fun `all-day UTC dates are independent of display timezone`() {
        zones.forEach { zone ->
            val occurrence = event(interval("2026-10-12T00:00:00Z", "2026-10-13T00:00:00Z", true, zone))
            assertEquals(CivilDate(2026, 10, 12), occurrence.date, zone)
            assertEquals("UTC", occurrence.start.timeZone.id)
            assertEquals(0, occurrence.start[GregorianCalendar.HOUR_OF_DAY])
            assertEquals(millis("2026-10-12T00:00:00Z"), occurrence.start.timeInMillis)
            assertNull(occurrence.time)
            assertNull(occurrence.interval.clipToDay(occurrence.date))
            val store = DeviceCalendarEventsStore(listOf(occurrence))
            assertTrue(store.getEvents(CivilDate(2026, 10, 11)).isEmpty())
            assertSame(occurrence, store.getEvents(CivilDate(2026, 10, 12)).single())
            assertTrue(store.getEvents(CivilDate(2026, 10, 13)).isEmpty())
        }
    }

    @Test
    fun `timed records preserve instants and use display timezone rules`() {
        zones.zip(listOf(CivilDate(2026, 10, 11), CivilDate(2026, 10, 12), CivilDate(2026, 10, 12)))
            .forEach { (zone, date) ->
                val occurrence = interval("2026-10-12T00:00:00Z", "2026-10-12T01:00:00Z", false, zone)
                assertEquals(date, occurrence.date, zone)
                assertEquals(zone, occurrence.start.timeZone.id)
                assertEquals(millis("2026-10-12T00:00:00Z"), occurrence.start.timeInMillis)
                assertEquals(millis("2026-10-12T01:00:00Z"), occurrence.end.timeInMillis)
                assertTrue(occurrence.occupies(date))
            }
    }

    @Test
    fun `three-day all-day occurrence occupies exactly three dates`() {
        zones.forEach { zone ->
            val occurrence = event(interval("2026-10-12T00:00:00Z", "2026-10-15T00:00:00Z", true, zone))
            val store = DeviceCalendarEventsStore(listOf(occurrence))
            (11..15).forEach { day ->
                assertEquals(if (day in 12..14) listOf(occurrence) else emptyList(), store.getEvents(CivilDate(2026, 10, day)), zone)
            }
            assertEquals(listOf(occurrence), store.getAllEvents())
            assertTrue(store.getEvents(CivilDate(2027, 10, 12)).isEmpty())
        }
    }

    @Test
    fun `overnight timed occurrence is clipped on both occupied days`() {
        zones.forEach { zone ->
            val first = CivilDate(2026, 10, 12)
            val second = CivilDate(2026, 10, 13)
            val occurrence = event(DeviceEventInterval(localMillis(first, zone, 23), localMillis(second, zone, 1), false, TimeZone.getTimeZone(zone)))
            val store = DeviceCalendarEventsStore(listOf(occurrence))
            assertSame(occurrence, store.getEvents(first).single())
            assertSame(occurrence, store.getEvents(second).single())
            val firstSegment = assertNotNull(occurrence.interval.clipToDay(first))
            val secondSegment = assertNotNull(occurrence.interval.clipToDay(second))
            assertEquals(23f, firstSegment.startHour)
            assertEquals(24f, firstSegment.endHour)
            assertEquals(0f, secondSegment.startHour)
            assertEquals(1f, secondSegment.endHour)
            assertEquals(firstSegment.endMillis, secondSegment.startMillis)
            assertNull(occurrence.interval.clipToDay(CivilDate(2026, 10, 14)))
        }
    }

    @Test
    fun `exclusive midnight end never occupies the following date`() {
        zones.forEach { zone ->
            val first = CivilDate(2026, 10, 12)
            val second = CivilDate(2026, 10, 13)
            val occurrence = event(DeviceEventInterval(localMillis(first, zone, 23), localMillis(second, zone), false, TimeZone.getTimeZone(zone)))
            val store = DeviceCalendarEventsStore(listOf(occurrence))
            assertEquals(1, store.getEvents(first).size)
            assertTrue(store.getEvents(second).isEmpty())
            assertEquals(24f, assertNotNull(occurrence.interval.clipToDay(first)).endHour)
            assertNull(occurrence.interval.clipToDay(second))
        }
    }

    @Test
    fun `membership crosses month and year boundaries without annual repetition`() {
        val boundaries = listOf(
            listOf(CivilDate(2026, 10, 31), CivilDate(2026, 11, 1), CivilDate(2026, 11, 2)),
            listOf(CivilDate(2026, 12, 31), CivilDate(2027, 1, 1), CivilDate(2027, 1, 2)),
        )
        zones.forEach { zone ->
            boundaries.forEach { (first, second, exclusiveEnd) ->
                listOf(true, false).forEach { allDay ->
                    val endpointZone = if (allDay) "UTC" else zone
                    val occurrence = event(DeviceEventInterval(localMillis(first, endpointZone, if (allDay) 0 else 23), localMillis(exclusiveEnd, endpointZone), allDay, TimeZone.getTimeZone(zone)))
                    val store = DeviceCalendarEventsStore(listOf(occurrence))
                    assertEquals(1, store.getEvents(first).size)
                    assertEquals(1, store.getEvents(second).size)
                    assertTrue(store.getEvents(exclusiveEnd).isEmpty())
                    assertTrue(store.getEvents(CivilDate(first.year + 1, first.month, first.dayOfMonth)).isEmpty())
                }
            }
        }
    }

    @Test
    fun `recurring provider instances retain occurrence identity and order`() {
        val first = event(interval("2026-10-12T23:00:00Z", "2026-10-13T01:00:00Z", false, "UTC"))
        val second = event(interval("2026-10-13T23:00:00Z", "2026-10-14T01:00:00Z", false, "UTC"))
        val third = event(interval("2026-10-13T12:00:00Z", "2026-10-13T13:00:00Z", false, "UTC"))
        val store = DeviceCalendarEventsStore(listOf(second, third, first))
        val middleDay = store.getEvents(CivilDate(2026, 10, 13))
        assertEquals(3, middleDay.size)
        assertSame(first, middleDay[0])
        assertSame(third, middleDay[1])
        assertSame(second, middleDay[2])
        assertEquals(3, store.getAllEvents().size)
    }

    @Test
    fun `23 and 25 hour DST days use calendar boundaries and exclusive ends`() {
        val zone = "America/Los_Angeles"
        listOf(
            Triple(CivilDate(2026, 3, 8), CivilDate(2026, 3, 9), 23),
            Triple(CivilDate(2026, 11, 1), CivilDate(2026, 11, 2), 25),
        ).forEach { (day, nextDay, hours) ->
            val begin = localMillis(day, zone)
            val end = localMillis(nextDay, zone)
            assertEquals(hours * 3_600_000L, end - begin)
            val occurrence = event(DeviceEventInterval(begin, end, false, TimeZone.getTimeZone(zone)))
            val store = DeviceCalendarEventsStore(listOf(occurrence))
            assertEquals(1, store.getEvents(day).size)
            assertTrue(store.getEvents(nextDay).isEmpty())
            val segment = assertNotNull(occurrence.interval.clipToDay(day))
            assertEquals(0f, segment.startHour)
            assertEquals(24f, segment.endHour)
            assertEquals(begin, segment.startMillis)
            assertEquals(end, segment.endMillis)
            val lateEvent = event(DeviceEventInterval(localMillis(day, zone, 23), localMillis(nextDay, zone, 1), false, TimeZone.getTimeZone(zone)))
            assertEquals(1f, assertNotNull(lateEvent.interval.clipToDay(nextDay)).endHour)
        }
    }

    @Test
    fun `midnight gap does not extend a day into the following midnight hour`() {
        val zone = "Africa/Cairo"
        val day = CivilDate(2026, 4, 24)
        val nextDay = CivilDate(2026, 4, 25)
        val begin = millis("2026-04-23T22:00:00Z") // First valid time is 01:00 local.
        val end = millis("2026-04-24T21:00:00Z") // Next day starts at 00:00 local.
        assertEquals(begin, DeviceEventInterval.startOfDay(day, TimeZone.getTimeZone(zone)).timeInMillis)
        assertEquals(end, DeviceEventInterval.startOfDay(nextDay, TimeZone.getTimeZone(zone)).timeInMillis)
        assertEquals(23 * 3_600_000L, end - begin)

        val wholeDay = DeviceEventInterval(begin, end, false, TimeZone.getTimeZone(zone))
        val segment = assertNotNull(wholeDay.clipToDay(day))
        assertEquals(begin, segment.startMillis)
        assertEquals(end, segment.endMillis)
        assertEquals(0f, segment.startHour)
        assertEquals(24f, segment.endHour)
        assertFalse(wholeDay.occupies(nextDay))
        assertNull(wholeDay.clipToDay(nextDay))

        val nextDayEvent = event(interval("2026-04-24T21:15:00Z", "2026-04-24T21:45:00Z", false, zone))
        val store = DeviceCalendarEventsStore(listOf(nextDayEvent))
        assertTrue(store.getEvents(day).isEmpty())
        assertNull(nextDayEvent.interval.clipToDay(day))
        assertSame(nextDayEvent, store.getEvents(nextDay).single())
        val nextSegment = assertNotNull(nextDayEvent.interval.clipToDay(nextDay))
        assertEquals(0.25f, nextSegment.startHour)
        assertEquals(0.75f, nextSegment.endHour)
    }

    @Test
    fun `both repeated midnight hours belong only to the overlap date`() {
        val zone = "America/Havana"
        val previousDay = CivilDate(2026, 10, 31)
        val day = CivilDate(2026, 11, 1)
        val nextDay = CivilDate(2026, 11, 2)
        val begin = millis("2026-11-01T04:00:00Z") // First occurrence of local midnight.
        val end = millis("2026-11-02T05:00:00Z")
        assertEquals(begin, DeviceEventInterval.startOfDay(day, TimeZone.getTimeZone(zone)).timeInMillis)
        assertEquals(end, DeviceEventInterval.startOfDay(nextDay, TimeZone.getTimeZone(zone)).timeInMillis)
        assertEquals(25 * 3_600_000L, end - begin)
        val wholeDay = DeviceEventInterval(begin, end, false, TimeZone.getTimeZone(zone))
        val wholeSegment = assertNotNull(wholeDay.clipToDay(day))
        assertEquals(begin, wholeSegment.startMillis)
        assertEquals(end, wholeSegment.endMillis)
        assertEquals(0f, wholeSegment.startHour)
        assertEquals(24f, wholeSegment.endHour)
        assertFalse(wholeDay.occupies(previousDay))
        assertFalse(wholeDay.occupies(nextDay))

        listOf(
            "2026-11-01T04:15:00Z" to "2026-11-01T04:45:00Z",
            "2026-11-01T05:15:00Z" to "2026-11-01T05:45:00Z",
        ).forEach { (start, finish) ->
            val occurrence = event(interval(start, finish, false, zone))
            val store = DeviceCalendarEventsStore(listOf(occurrence))
            assertTrue(store.getEvents(previousDay).isEmpty())
            assertSame(occurrence, store.getEvents(day).single())
            assertTrue(store.getEvents(nextDay).isEmpty())
            assertNull(occurrence.interval.clipToDay(previousDay))
            val segment = assertNotNull(occurrence.interval.clipToDay(day))
            assertEquals(millis(start), segment.startMillis)
            assertEquals(millis(finish), segment.endMillis)
            assertEquals(0.25f, segment.startHour)
            assertEquals(0.75f, segment.endHour)
        }
    }

    @Test
    fun `exclusive end at a midnight transition does not occupy the transition date`() {
        listOf(
            Triple("Africa/Cairo", CivilDate(2026, 4, 24), "2026-04-23T22:00:00Z"),
            Triple("America/Havana", CivilDate(2026, 11, 1), "2026-11-01T04:00:00Z"),
        ).forEach { (zone, day, boundary) ->
            val end = millis(boundary)
            val previousDay = CivilDate(day.toJdn() - 1)
            val occurrence = event(DeviceEventInterval(end - 3_600_000L, end, false, TimeZone.getTimeZone(zone)))
            val store = DeviceCalendarEventsStore(listOf(occurrence))
            assertSame(occurrence, store.getEvents(previousDay).single())
            assertTrue(store.getEvents(day).isEmpty())
            val segment = assertNotNull(occurrence.interval.clipToDay(previousDay))
            assertEquals(end, segment.endMillis)
            assertEquals(24f, segment.endHour)
            assertNull(occurrence.interval.clipToDay(day))
        }
    }

    @Test
    fun `gap spanning midnight starts at the first valid time`() {
        val zone = TimeZone.getTimeZone("America/Toronto")
        val day = CivilDate(1919, 3, 31)
        // Clocks jumped from 23:30 to 00:30, so lenient 00:00 normalizes past the day start.
        val start = DeviceEventInterval.startOfDay(day, zone)
        assertEquals(millis("1919-03-31T04:30:00Z"), start.timeInMillis)
        assertEquals(0, start[GregorianCalendar.HOUR_OF_DAY])
        assertEquals(30, start[GregorianCalendar.MINUTE])
        val occurrence = interval("1919-03-31T04:30:00Z", "1919-03-31T04:45:00Z", false, zone.id)
        assertTrue(occurrence.occupies(day))
        assertFalse(occurrence.occupies(CivilDate(1919, 3, 30)))
        assertEquals(millis("1919-03-31T04:30:00Z"), assertNotNull(occurrence.clipToDay(day)).startMillis)
    }

    @Test
    fun `skipped civil date has no occurrences or clipped segments`() {
        val zone = TimeZone.getTimeZone("Pacific/Apia")
        val previousDay = CivilDate(2011, 12, 29)
        val skippedDay = CivilDate(2011, 12, 30)
        val nextDay = CivilDate(2011, 12, 31)
        val boundary = millis("2011-12-30T10:00:00Z")
        assertEquals(boundary, DeviceEventInterval.startOfDay(skippedDay, zone).timeInMillis)
        assertEquals(boundary, DeviceEventInterval.startOfDay(nextDay, zone).timeInMillis)
        val occurrence = event(interval("2011-12-29T22:00:00Z", "2011-12-30T12:00:00Z", false, zone.id))
        val store = DeviceCalendarEventsStore(listOf(occurrence))
        assertSame(occurrence, store.getEvents(previousDay).single())
        assertTrue(store.getEvents(skippedDay).isEmpty())
        assertSame(occurrence, store.getEvents(nextDay).single())
        assertNull(occurrence.interval.clipToDay(skippedDay))
        assertEquals(boundary, assertNotNull(occurrence.interval.clipToDay(previousDay)).endMillis)
        assertEquals(boundary, assertNotNull(occurrence.interval.clipToDay(nextDay)).startMillis)
        val point = DeviceEventInterval(boundary, boundary, false, zone)
        assertFalse(point.occupies(skippedDay))
        assertTrue(point.occupies(nextDay))
    }

    @Test
    fun `multi-day all-day and timed occurrences cross both DST transitions`() {
        val zone = "America/Los_Angeles"
        listOf(
            listOf(CivilDate(2026, 3, 7), CivilDate(2026, 3, 8), CivilDate(2026, 3, 9), CivilDate(2026, 3, 10)),
            listOf(CivilDate(2026, 10, 31), CivilDate(2026, 11, 1), CivilDate(2026, 11, 2), CivilDate(2026, 11, 3)),
        ).forEach { dates ->
            listOf(true, false).forEach { allDay ->
                val endpointZone = if (allDay) "UTC" else zone
                val occurrence = event(DeviceEventInterval(
                    localMillis(dates.first(), endpointZone), localMillis(dates.last(), endpointZone),
                    allDay, TimeZone.getTimeZone(zone),
                ))
                val store = DeviceCalendarEventsStore(listOf(occurrence))
                dates.dropLast(1).forEach { date -> assertSame(occurrence, store.getEvents(date).single()) }
                assertTrue(store.getEvents(dates.last()).isEmpty())
                if (!allDay) {
                    val transitionDay = assertNotNull(occurrence.interval.clipToDay(dates[1]))
                    assertEquals(0f, transitionDay.startHour)
                    assertEquals(24f, transitionDay.endHour)
                }
            }
        }
    }

    @Test
    fun `repeated DST hour does not produce a negative clock span`() {
        val occurrence = interval("2026-11-01T08:45:00Z", "2026-11-01T09:15:00Z", false, "America/Los_Angeles")
        val segment = assertNotNull(occurrence.clipToDay(CivilDate(2026, 11, 1)))
        assertEquals(1.75f, segment.startHour)
        assertEquals(2.25f, segment.endHour)
        assertEquals(30 * 60_000L, segment.endMillis - segment.startMillis)
    }

    @Test
    fun `zero-duration and missing-end timed events remain on their start day`() {
        val begin = millis("2026-10-12T23:00:00Z")
        listOf(begin, 0L).forEach { end ->
            val occurrence = DeviceEventInterval(begin, end, false, TimeZone.getTimeZone("UTC"))
            assertTrue(occurrence.occupies(CivilDate(2026, 10, 12)))
            assertFalse(occurrence.occupies(CivilDate(2026, 10, 13)))
            val segment = assertNotNull(occurrence.clipToDay(CivilDate(2026, 10, 12)))
            assertEquals(23f, segment.startHour)
            assertEquals(23f, segment.endHour)
        }
    }
}
