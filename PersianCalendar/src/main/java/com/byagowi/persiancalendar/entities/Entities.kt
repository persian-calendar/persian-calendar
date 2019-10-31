package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.utils.CalendarType
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinate
import java.util.*

abstract class BaseEvent(open val title: String, open val isHoliday: Boolean)

data class GregorianCalendarEvent(
    val date: CivilDate, override val title: String,
    override val isHoliday: Boolean
) : BaseEvent(title, isHoliday) {
    override fun toString(): String = title
}

data class IslamicCalendarEvent(
    val date: IslamicDate, override val title: String,
    override val isHoliday: Boolean
) : BaseEvent(title, isHoliday) {
    override fun toString(): String = title
}

data class PersianCalendarEvent(
    val date: PersianDate, override val title: String,
    override val isHoliday: Boolean
) : BaseEvent(title, isHoliday) {
    override fun toString(): String = title
}

class DeviceCalendarEvent(
    val id: Int, override val title: String, val description: String,
    val start: Date, val end: Date, val dateString: String, val date: CivilDate,
    val color: String, override val isHoliday: Boolean
) : BaseEvent(title, isHoliday) {
    override fun toString(): String = title
}

data class ShiftWorkRecord(val type: String, val length: Int)

data class CityItem(
    val key: String, val en: String, val fa: String, val ckb: String, val ar: String,
    val countryCode: String, val countryEn: String, val countryFa: String, val countryCkb: String,
    val countryAr: String, val coordinate: Coordinate
)

data class DayItem(val isToday: Boolean, val jdn: Long, val dayOfWeek: Int)

data class CalendarTypeItem(val type: CalendarType, private val title: String) {
    override fun toString(): String = title
}

data class StringWithValueItem(val value: Int, private val title: String) {
    override fun toString(): String = title
}