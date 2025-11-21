package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.generated.EventSource
import com.byagowi.persiancalendar.global.language
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.GregorianCalendar

sealed class CalendarEvent<T : AbstractDate>(
    val title: String, val isHoliday: Boolean, val date: T, val source: EventSource?
) {
    class GregorianCalendarEvent(
        title: String, isHoliday: Boolean, date: CivilDate, source: EventSource?,
    ) : CalendarEvent<CivilDate>(title, isHoliday, date, source)

    class IslamicCalendarEvent(
        title: String, isHoliday: Boolean, date: IslamicDate, source: EventSource?,
    ) : CalendarEvent<IslamicDate>(title, isHoliday, date, source)

    class PersianCalendarEvent(
        title: String, isHoliday: Boolean, date: PersianDate, source: EventSource?,
    ) : CalendarEvent<PersianDate>(title, isHoliday, date, source)

    class EquinoxCalendarEvent(
        title: String, isHoliday: Boolean, date: PersianDate, source: EventSource?,
        val remainingMillis: Long
    ) : CalendarEvent<PersianDate>(title, isHoliday, date, source)

    class NepaliCalendarEvent(
        title: String, isHoliday: Boolean, date: NepaliDate, source: EventSource?
    ) : CalendarEvent<NepaliDate>(title, isHoliday, date, source)

    class DeviceCalendarEvent(
        date: CivilDate, title: String, isHoliday: Boolean, source: EventSource?,
        val id: Long, val description: String, val start: GregorianCalendar,
        val end: GregorianCalendar, val color: String, val time: String?,
    ) : CalendarEvent<CivilDate>(title, isHoliday, date, source)

    val oneLinerTitleWithTime
        get() = when (this) {
            is DeviceCalendarEvent -> if (time == null) title else {
                language.value.inParentheses.format(title, time)
            }

            else -> title
        }

    override fun equals(other: Any?): Boolean {
        return other is CalendarEvent<*>
                && other.title == title && other.isHoliday == isHoliday && other.date == date && (
                if (this is EquinoxCalendarEvent && other is EquinoxCalendarEvent)
                    remainingMillis == other.remainingMillis
                else true)
        // Let's don't get into details of device calendar
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + isHoliday.hashCode()
        result = 31 * result + date.hashCode()
        if (this is EquinoxCalendarEvent) result = 31 * result + remainingMillis.hashCode()
        return result
    }
}
