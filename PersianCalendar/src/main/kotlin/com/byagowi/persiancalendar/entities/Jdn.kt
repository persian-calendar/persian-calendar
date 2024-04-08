package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.global.weekEnds
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.ceil

// Julian day number, basically a day counter starting from some day in concept
// https://en.wikipedia.org/wiki/Julian_day
@JvmInline
value class Jdn(val value: Long) {
    constructor(value: AbstractDate) : this(value.toJdn())
    constructor(calendar: Calendar, year: Int, month: Int, day: Int) :
            this(calendar.createDate(year, month, day))

    // 0 means Saturday in it, see #`test day of week from jdn`() in the testsuite
    val weekDay: Int get() = ((value + 2L) % 7L).toInt()

    val isWeekEnd get() = weekEnds[this.weekDay]

    fun inCalendar(calendar: Calendar): AbstractDate = when (calendar) {
        Calendar.ISLAMIC -> toIslamicDate()
        Calendar.GREGORIAN -> toCivilDate()
        Calendar.SHAMSI -> toPersianDate()
        Calendar.NEPALI -> toNepaliDate()
    }

    fun toIslamicDate() = IslamicDate(value)
    fun toCivilDate() = CivilDate(value)
    fun toPersianDate() = PersianDate(value)
    fun toNepaliDate() = NepaliDate(value)

    operator fun compareTo(other: Jdn) = value.compareTo(other.value)
    operator fun plus(other: Int): Jdn = Jdn(value + other)
    operator fun minus(other: Int): Jdn = Jdn(value - other)

    // Difference of two Jdn values in days
    operator fun minus(other: Jdn): Int = (value - other.value).toInt()

    fun toGregorianCalendar(): GregorianCalendar = GregorianCalendar().also {
        val gregorian = this.toCivilDate()
        it.set(gregorian.year, gregorian.month - 1, gregorian.dayOfMonth)
    }

    fun getWeekOfYear(startOfYear: Jdn): Int {
        val dayOfYear = this - startOfYear
        return ceil(1 + (dayOfYear - applyWeekStartOffsetToWeekDay(this.weekDay)) / 7.0).toInt()
    }

    val weekDayName: String get() = weekDays[this.weekDay]

    // Days passed in a season and total days available in the season
    fun seasonPassedDaysAndDaysCount(): Pair<Int, Int> {
        val persianDate = this.toPersianDate()
        val season = (persianDate.month - 1) / 3
        val seasonBeginning = PersianDate(persianDate.year, season * 3 + 1, 1)
        val seasonBeginningJdn = Jdn(seasonBeginning)
        return this - seasonBeginningJdn + 1 to
                Jdn(seasonBeginning.monthStartOfMonthsDistance(3)) - seasonBeginningJdn
    }

    operator fun rangeTo(toJdn: Jdn): Sequence<Jdn> =
        (this.value..toJdn.value).asSequence().map(::Jdn)

    companion object {
        fun today() = Jdn(Date().toGregorianCalendar().toCivilDate())
    }
}
