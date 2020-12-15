package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.utils.CalendarType
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinate
import java.util.*

interface CalendarEvent<T : AbstractDate> {
    val title: String
    val isHoliday: Boolean
    val date: T
}

data class GregorianCalendarEvent(
        override val date: CivilDate, override val title: String, override val isHoliday: Boolean
) : CalendarEvent<CivilDate> {
    override fun toString(): String = title
}

data class IslamicCalendarEvent(
        override val date: IslamicDate, override val title: String, override val isHoliday: Boolean
) : CalendarEvent<IslamicDate> {
    override fun toString(): String = title
}

data class PersianCalendarEvent(
        override val date: PersianDate, override val title: String, override val isHoliday: Boolean
) : CalendarEvent<PersianDate> {
    override fun toString(): String = title
}

data class DeviceCalendarEvent(
        override val date: CivilDate, override val title: String, override val isHoliday: Boolean,
        val id: Int, val description: String, val start: Date, val end: Date, val color: String
) : CalendarEvent<CivilDate> {
    override fun toString(): String = "$title ($description)"
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