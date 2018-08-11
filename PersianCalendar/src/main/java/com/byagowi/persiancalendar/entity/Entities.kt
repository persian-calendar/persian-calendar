package com.byagowi.persiancalendar.entity

import calendar.CivilDate
import calendar.IslamicDate
import calendar.PersianDate
import com.github.praytimes.Coordinate
import java.util.*

interface BaseEvent {
  val title: String
  val isHoliday: Boolean
}

data class DeviceCalendarEvent(val id: Int, override val title: String, val description: String,
                               val start: Date, val end: Date, val date: String, val civilDate: CivilDate,
                               val color: String, override val isHoliday: Boolean) : BaseEvent

data class GregorianCalendarEvent(val date: CivilDate, override val title: String,
                                  override val isHoliday: Boolean) : BaseEvent

data class IslamicCalendarEvent(val date: IslamicDate, override val title: String,
                                override val isHoliday: Boolean) : BaseEvent

data class PersianCalendarEvent(val date: PersianDate, override val title: String,
                                override val isHoliday: Boolean) : BaseEvent

data class DayEntity(val jdn: Long, val today: Boolean, val dayOfWeek: Int)

data class CityEntity(val key: String, val en: String, val fa: String, val ckb: String,
                      val countryCode: String,
                      val countryEn: String, val countryFa: String, val countryCkb: String,
                      val coordinate: Coordinate)