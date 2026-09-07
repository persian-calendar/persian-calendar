package io.github.persiancalendar.calendar.util

import io.github.persiancalendar.calendar.AbstractDate

internal object TwelveMonthsYear {
    fun <T : AbstractDate> monthStartOfMonthsDistance(
        baseDate: T,
        monthsDistance: Int,
        createDate: (year: Int, month: Int, dayOfMonth: Int) -> T,
    ): T {
        var month =
            monthsDistance + baseDate.month - 1 // make it zero based for easier calculations
        var year = baseDate.year + (month / 12)
        month %= 12
        if (month < 0) {
            year -= 1
            month += 12
        }
        return createDate(year, month + 1, 1)
    }

    fun <T : AbstractDate> monthsDistanceTo(baseDate: T, toDate: T): Int =
        ((toDate.year - baseDate.year) * 12) + toDate.month - baseDate.month
}
