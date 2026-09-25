package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.serialization.Serializable
import kotlin.math.ceil

// Julian day number, basically a day counter starting from some day in concept
// https://en.wikipedia.org/wiki/Julian_day
//@Parcelize
@Serializable
@kotlin.jvm.JvmInline
value class Jdn(val value: Long) {
    constructor(value: AbstractDate) : this(value.toJdn())
    constructor(
        calendar: Calendar,
        year: Int,
        month: Int,
        day: Int,
    ) : this(calendar.createDate(year, month, day))

    val weekDay: WeekDay get() = WeekDay.entries[((value + 2L) % 7L).toInt()]

    infix fun on(calendar: Calendar): AbstractDate = when (calendar) {
        Calendar.ISLAMIC -> toIslamicDate()
        Calendar.GREGORIAN -> toCivilDate()
        Calendar.SHAMSI -> toPersianDate()
        Calendar.NEPALI -> toNepaliDate()
    }

    fun toIslamicDate() = IslamicDate(value)
    fun toCivilDate() = CivilDate(value)
    fun toPersianDate() = PersianDate(value)
    fun toNepaliDate() = NepaliDate(value)

    operator fun compareTo(other: Jdn) = value compareTo other.value
    operator fun plus(other: Int): Jdn = Jdn(value + other)
    operator fun minus(other: Int): Jdn = Jdn(value - other)

    // Difference of two Jdn values in days
    operator fun minus(other: Jdn): Int = (value - other.value).toInt()

    fun getWeekOfYear(startOfYear: Jdn, weekStart: WeekDay): Int {
        val dayOfYear = this - startOfYear
        return ceil(1 + (dayOfYear - (this.weekDay - weekStart)) / 7.0).toInt()
    }

    // Days passed in a season and total days available in the season
    // The result is a (passedDaysInSeason, totalSeasonDays)
    fun getPositionInSeason(): Pair<Int, Int> {
        val persianDate = this.toPersianDate()
        val season = (persianDate.month - 1) / 3
        val seasonBeginning = PersianDate(persianDate.year, season * 3 + 1, 1)
        val seasonBeginningJdn = Jdn(seasonBeginning)
        return this - seasonBeginningJdn + 1 to Jdn(seasonBeginning.monthStartOfMonthsDistance(3)) - seasonBeginningJdn
    }

    operator fun rangeTo(that: Jdn): Sequence<Jdn> =
        (this.value..that.value).asSequence().map(::Jdn)

    operator fun rangeUntil(that: Jdn): Sequence<Jdn> =
        (this.value..<that.value).asSequence().map(::Jdn)

    val isYearSupportedOnApp get() = (this.toPersianDate().year - supportedYearOfIranCalendar) in -1..0
}
