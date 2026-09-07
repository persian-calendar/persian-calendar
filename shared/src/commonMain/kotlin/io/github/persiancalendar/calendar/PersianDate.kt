package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.persian.LookupTableConverter
import io.github.persiancalendar.calendar.persian.OldEraConverter
import io.github.persiancalendar.calendar.util.TwelveMonthsYear.monthStartOfMonthsDistance
import io.github.persiancalendar.calendar.util.TwelveMonthsYear.monthsDistanceTo
import io.github.persiancalendar.calendar.util.jdnFromPersian
import io.github.persiancalendar.calendar.util.persianFromJdn

class PersianDate : AbstractDate, YearMonthDate<PersianDate> {
    constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)
    constructor(date: AbstractDate) : super(date)
    constructor(jdn: Long) : super(jdn)

    // Converters
    override fun toJdn(): Long {
        var result = LookupTableConverter.toJdn(year, month, dayOfMonth)
        if (result == -1L) result = OldEraConverter.toJdn(year, month, dayOfMonth)
        if (result == -1L) result = jdnFromPersian(year, month, dayOfMonth)
        return result
    }

    override fun monthStartOfMonthsDistance(monthsDistance: Int): PersianDate =
        monthStartOfMonthsDistance(this, monthsDistance, ::PersianDate)

    override fun monthsDistanceTo(date: PersianDate): Int = monthsDistanceTo(this, date)

    override fun toString(): String = "PersianDate($year, $month, $dayOfMonth)"

    override fun fromJdn(jdn: Long): DateTriplet =
        LookupTableConverter.fromJdn(jdn) ?: OldEraConverter.fromJdn(jdn) ?: persianFromJdn(jdn)

    companion object {
        // This is always Borji (old era) and never new era's fixed months days
        fun borjiFromJdn(jdn: Long): DateTriplet =
            OldEraConverter.fromJdn(jdn) ?: persianFromJdn(jdn, alwaysBorji = true)

        // First six months have length of 31, next 5 months are 30 and the last month is 29 and in leap years are 30
        private val daysToMonth =
            intArrayOf(0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 366)

        internal fun monthFromDaysCount(days: Int): Int = daysToMonth.indexOfFirst { it >= days }
        internal fun daysInPreviousMonths(month: Int): Int = daysToMonth[month - 1]
    }
}
