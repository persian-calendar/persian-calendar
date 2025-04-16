package com.byagowi.persiancalendar

import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.GregorianCalendar

// Julian day number, basically a day counter starting from some day in concept
// https://en.wikipedia.org/wiki/Julian_day
@JvmInline
value class Jdn(val value: Long) {
    constructor(value: AbstractDate) : this(value.toJdn())

    fun toIslamicDate() = IslamicDate(value)
    fun toCivilDate() = CivilDate(value)
    fun toPersianDate() = PersianDate(value)

    operator fun compareTo(other: Jdn) = value compareTo other.value
    operator fun plus(other: Int): Jdn = Jdn(value + other)
    operator fun minus(other: Int): Jdn = Jdn(value - other)

    // Difference of two Jdn values in days
    operator fun minus(other: Jdn): Int = (value - other.value).toInt()

    operator fun rangeTo(that: Jdn): Sequence<Jdn> =
        (this.value..that.value).asSequence().map(::Jdn)

    operator fun rangeUntil(that: Jdn): Sequence<Jdn> =
        (this.value..<that.value).asSequence().map(::Jdn)

    companion object {
        fun today(): Jdn {
            val calendar = GregorianCalendar.getInstance()
            val jdn = CivilDate(
                calendar[GregorianCalendar.YEAR],
                calendar[GregorianCalendar.MONTH] + 1,
                calendar[GregorianCalendar.DAY_OF_MONTH],
            )
            return Jdn(jdn)
        }
    }
}
