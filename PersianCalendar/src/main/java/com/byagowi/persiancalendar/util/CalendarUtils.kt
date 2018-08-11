package com.byagowi.persiancalendar.util

import calendar.*
import java.util.*

object CalendarUtils {

  val persianToday: PersianDate
    get() = DateConverter.civilToPersian(CivilDate(makeCalendarFromDate(Date())))

  val todayJdn: Long
    get() = DateConverter.civilToJdn(CivilDate(makeCalendarFromDate(Date())))

  val islamicToday: IslamicDate
    get() = DateConverter.civilToIslamic(CivilDate(makeCalendarFromDate(Date())), Utils.getIslamicOffset())

  val gregorianToday: CivilDate
    get() = CivilDate(makeCalendarFromDate(Date()))

  fun getDateOfCalendar(calendar: CalendarType, year: Int, month: Int, day: Int): AbstractDate =
      when (calendar) {
        CalendarType.ISLAMIC -> IslamicDate(year, month, day)
        CalendarType.GREGORIAN -> CivilDate(year, month, day)
        CalendarType.SHAMSI -> PersianDate(year, month, day)
        else -> PersianDate(year, month, day)
      }

  fun getJdnOfCalendar(calendar: CalendarType, year: Int, month: Int, day: Int): Long =
      when (calendar) {
        CalendarType.ISLAMIC -> DateConverter.islamicToJdn(year, month, day)
        CalendarType.GREGORIAN -> DateConverter.civilToJdn(year.toLong(), month.toLong(), day.toLong())
        CalendarType.SHAMSI -> DateConverter.persianToJdn(year, month, day)
      }

  fun getDateFromJdnOfCalendar(calendar: CalendarType, jdn: Long): AbstractDate = when (calendar) {
    CalendarType.ISLAMIC -> DateConverter.jdnToIslamic(jdn)
    CalendarType.GREGORIAN -> DateConverter.jdnToCivil(jdn)
    CalendarType.SHAMSI -> DateConverter.jdnToPersian(jdn)
  }

  fun getJdnDate(date: AbstractDate): Long = if (date is PersianDate) {
    DateConverter.persianToJdn(date)
  } else if (date is IslamicDate) {
    DateConverter.islamicToJdn(date)
  } else if (date is CivilDate) {
    DateConverter.civilToJdn(date)
  } else {
    0
  }

  fun makeCalendarFromDate(date: Date): Calendar {
    val calendar = Calendar.getInstance()
    if (Utils.isIranTime) {
      calendar.timeZone = TimeZone.getTimeZone("Asia/Tehran")
    }
    calendar.time = date
    return calendar
  }

  fun toLinearDate(date: AbstractDate): String =
      String.format("%s/%s/%s", Utils.formatNumber(date.year),
          Utils.formatNumber(date.month), Utils.formatNumber(date.dayOfMonth))

  fun getTodayOfCalendar(calendar: CalendarType): AbstractDate = when (calendar) {
    CalendarType.ISLAMIC -> islamicToday
    CalendarType.GREGORIAN -> gregorianToday
    CalendarType.SHAMSI -> persianToday
  }

  fun dateStringOfOtherCalendar(calendar: CalendarType, jdn: Long): String = when (calendar) {
    CalendarType.ISLAMIC -> Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
        Utils.comma + " " +
        Utils.dateToString(DateConverter.jdnToCivil(jdn))
    CalendarType.GREGORIAN -> Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
        Utils.comma + " " +
        Utils.dateToString(DateConverter.civilToIslamic(
            DateConverter.jdnToCivil(jdn), Utils.getIslamicOffset()))
    CalendarType.SHAMSI -> Utils.dateToString(DateConverter.jdnToCivil(jdn)) +
        Utils.comma + " " +
        Utils.dateToString(DateConverter.civilToIslamic(
            DateConverter.jdnToCivil(jdn), Utils.getIslamicOffset()))
  }

  fun dayTitleSummary(date: AbstractDate): String = Utils.getWeekDayName(date) + Utils.comma + " " + Utils.dateToString(date)

  fun getMonthName(date: AbstractDate): String = Utils.monthsNamesOfCalendar(date)[date.month - 1]

  fun getDayOfWeekFromJdn(jdn: Long): Int = DateConverter.jdnToCivil(jdn).dayOfWeek % 7

  // based on R.array.calendar_type order
  fun calendarTypeFromPosition(position: Int): CalendarType = when (position) {
    0 -> CalendarType.SHAMSI
    1 -> CalendarType.ISLAMIC
    else -> CalendarType.GREGORIAN
  }

  fun positionFromCalendarType(calendar: CalendarType): Int = when (calendar) {
    CalendarType.SHAMSI -> 0
    CalendarType.ISLAMIC -> 1
    else -> 2
  }
}
