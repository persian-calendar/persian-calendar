package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.utils.CalendarType
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinate
import java.util.*

sealed class CalendarEvent<T : AbstractDate>(
    val title: String, val isHoliday: Boolean, val date: T
) {
    class GregorianCalendarEvent(title: String, isHoliday: Boolean, date: CivilDate) :
        CalendarEvent<CivilDate>(title, isHoliday, date)

    class IslamicCalendarEvent(title: String, isHoliday: Boolean, date: IslamicDate) :
        CalendarEvent<IslamicDate>(title, isHoliday, date)

    class PersianCalendarEvent(title: String, isHoliday: Boolean, date: PersianDate) :
        CalendarEvent<PersianDate>(title, isHoliday, date)

    class DeviceCalendarEvent(
        date: CivilDate, title: String, isHoliday: Boolean, val id: Int, val description: String,
        val start: Date, val end: Date, val color: String
    ) : CalendarEvent<CivilDate>(title, isHoliday, date)
}

data class ShiftWorkRecord(val type: String, val length: Int)

data class CityItem(
    val key: String, val en: String, val fa: String, val ckb: String, val ar: String,
    val countryCode: String, val countryEn: String, val countryFa: String, val countryCkb: String,
    val countryAr: String, val coordinate: Coordinate
)

data class CalendarTypeItem(val type: CalendarType, private val title: String) {
    override fun toString(): String = title
}