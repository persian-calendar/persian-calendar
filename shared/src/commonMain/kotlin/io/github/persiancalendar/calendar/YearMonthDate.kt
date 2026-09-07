package io.github.persiancalendar.calendar

internal interface YearMonthDate<T : AbstractDate> {
    // Ideally getYear()/getMonth()/getDay() also should be moved to this interface
    fun monthStartOfMonthsDistance(monthsDistance: Int): T
    fun monthsDistanceTo(date: T): Int
}
