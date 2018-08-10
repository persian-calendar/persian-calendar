package com.byagowi.persiancalendar.util

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

import calendar.AbstractDate
import calendar.CalendarType
import calendar.CivilDate
import calendar.DateConverter
import calendar.IslamicDate
import calendar.PersianDate

object CalendarUtils {

  val persianToday: PersianDate
    get() = DateConverter.civilToPersian(CivilDate(makeCalendarFromDate(Date())))

  val todayJdn: Long
    get() = DateConverter.civilToJdn(CivilDate(makeCalendarFromDate(Date())))

  val islamicToday: IslamicDate
    get() = DateConverter.civilToIslamic(CivilDate(makeCalendarFromDate(Date())), Utils.getIslamicOffset())

  val gregorianToday: CivilDate
    get() = CivilDate(makeCalendarFromDate(Date()))

  fun getDateOfCalendar(calendar: CalendarType, year: Int, month: Int, day: Int): AbstractDate {
    when (calendar) {
      CalendarType.ISLAMIC -> return IslamicDate(year, month, day)
      CalendarType.GREGORIAN -> return CivilDate(year, month, day)
      CalendarType.SHAMSI -> return PersianDate(year, month, day)
      else -> return PersianDate(year, month, day)
    }
  }

  fun getJdnOfCalendar(calendar: CalendarType, year: Int, month: Int, day: Int): Long {
    when (calendar) {
      CalendarType.ISLAMIC -> return DateConverter.islamicToJdn(year, month, day)
      CalendarType.GREGORIAN -> return DateConverter.civilToJdn(year.toLong(), month.toLong(), day.toLong())
      CalendarType.SHAMSI -> return DateConverter.persianToJdn(year, month, day)
      else -> return DateConverter.persianToJdn(year, month, day)
    }
  }

  fun getDateFromJdnOfCalendar(calendar: CalendarType, jdn: Long): AbstractDate {
    when (calendar) {
      CalendarType.ISLAMIC -> return DateConverter.jdnToIslamic(jdn)
      CalendarType.GREGORIAN -> return DateConverter.jdnToCivil(jdn)
      CalendarType.SHAMSI -> return DateConverter.jdnToPersian(jdn)
      else -> return DateConverter.jdnToPersian(jdn)
    }
  }

  fun getJdnDate(date: AbstractDate): Long {
    return if (date is PersianDate) {
      DateConverter.persianToJdn(date)
    } else if (date is IslamicDate) {
      DateConverter.islamicToJdn(date)
    } else if (date is CivilDate) {
      DateConverter.civilToJdn(date)
    } else {
      0
    }
  }

  fun makeCalendarFromDate(date: Date): Calendar {
    val calendar = Calendar.getInstance()
    if (Utils.isIranTime) {
      calendar.timeZone = TimeZone.getTimeZone("Asia/Tehran")
    }
    calendar.time = date
    return calendar
  }

  fun toLinearDate(date: AbstractDate): String {
    return String.format("%s/%s/%s", Utils.formatNumber(date.year),
        Utils.formatNumber(date.month), Utils.formatNumber(date.dayOfMonth))
  }

  fun getTodayOfCalendar(calendar: CalendarType): AbstractDate {
    when (calendar) {
      CalendarType.ISLAMIC -> return islamicToday
      CalendarType.GREGORIAN -> return gregorianToday
      CalendarType.SHAMSI -> return persianToday
      else -> return persianToday
    }
  }

  fun dateStringOfOtherCalendar(calendar: CalendarType, jdn: Long): String {
    when (calendar) {
      CalendarType.ISLAMIC -> return Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
          Utils.comma + " " +
          Utils.dateToString(DateConverter.jdnToCivil(jdn))
      CalendarType.GREGORIAN -> return Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
          Utils.comma + " " +
          Utils.dateToString(DateConverter.civilToIslamic(
              DateConverter.jdnToCivil(jdn), Utils.getIslamicOffset()))
      CalendarType.SHAMSI -> return Utils.dateToString(DateConverter.jdnToCivil(jdn)) +
          Utils.comma + " " +
          Utils.dateToString(DateConverter.civilToIslamic(
              DateConverter.jdnToCivil(jdn), Utils.getIslamicOffset()))
      else -> return Utils.dateToString(DateConverter.jdnToCivil(jdn)) + Utils.comma + " " + Utils.dateToString(DateConverter.civilToIslamic(DateConverter.jdnToCivil(jdn), Utils.getIslamicOffset()))
    }
  }

  fun dayTitleSummary(date: AbstractDate): String {
    return Utils.getWeekDayName(date) + Utils.comma + " " + Utils.dateToString(date)
  }

  fun getMonthName(date: AbstractDate): String {
    return Utils.monthsNamesOfCalendar(date)[date.month - 1]
  }

  fun getDayOfWeekFromJdn(jdn: Long): Int {
    return DateConverter.jdnToCivil(jdn).dayOfWeek % 7
  }

  // based on R.array.calendar_type order
  fun calendarTypeFromPosition(position: Int): CalendarType {
    when (position) {
      0 -> return CalendarType.SHAMSI
      1 -> return CalendarType.ISLAMIC
      else -> return CalendarType.GREGORIAN
    }
  }

  fun positionFromCalendarType(calendar: CalendarType): Int {
    when (calendar) {
      CalendarType.SHAMSI -> return 0
      CalendarType.ISLAMIC -> return 1
      else -> return 2
    }
  }
}
