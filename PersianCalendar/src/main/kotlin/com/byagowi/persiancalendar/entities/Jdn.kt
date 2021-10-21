package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toJavaCalendar
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.*
import kotlin.math.ceil

// Julian day number, basically a day counter starting from some day in concept
// https://en.wikipedia.org/wiki/Julian_day
@JvmInline
value class Jdn(val value: Long) {
    constructor(value: AbstractDate) : this(value.toJdn())
    constructor(calendar: CalendarType, year: Int, month: Int, day: Int) :
            this(calendar.createDate(year, month, day))

    // 0 means Saturday in it, see #`test day of week from jdn`() in the testsuite
    val dayOfWeek: Int get() = ((value + 2L) % 7L).toInt()

    fun toCalendar(calendar: CalendarType): AbstractDate = when (calendar) {
        CalendarType.ISLAMIC -> toIslamicCalendar()
        CalendarType.GREGORIAN -> toGregorianCalendar()
        CalendarType.SHAMSI -> toPersianCalendar()
    }

    fun toIslamicCalendar() = IslamicDate(value)
    fun toGregorianCalendar() = CivilDate(value)
    fun toPersianCalendar() = PersianDate(value)

    fun createMonthDaysList(monthLength: Int) = (value until value + monthLength).map(::Jdn)

    operator fun compareTo(other: Jdn) = value.compareTo(other.value)
    operator fun plus(other: Int): Jdn = Jdn(value + other)
    operator fun minus(other: Int): Jdn = Jdn(value - other)

    // Difference of two Jdn values in days
    operator fun minus(other: Jdn): Int = (value - other.value).toInt()

    fun toJavaCalendar(): GregorianCalendar = GregorianCalendar().also {
        val gregorian = this.toGregorianCalendar()
        it.set(gregorian.year, gregorian.month - 1, gregorian.dayOfMonth)
    }

    fun getWeekOfYear(startOfYear: Jdn): Int {
        val dayOfYear = this - startOfYear
        return ceil(1 + (dayOfYear - applyWeekStartOffsetToWeekDay(this.dayOfWeek)) / 7.0).toInt()
    }

    val dayOfWeekName: String get() = weekDays[this.dayOfWeek]

    companion object {
        fun today() = Jdn(Date().toJavaCalendar().toCivilDate())
    }
}
