package com.byagowi.persiancalendar.ui.calendar.reports

import com.byagowi.persiancalendar.entities.Calendar
import io.github.persiancalendar.calendar.AbstractDate

// Use the same one-based date for both the calculation and the row heading.
internal fun prayTimeReportMonthDates(calendar: Calendar, date: AbstractDate): List<AbstractDate> =
    (1..calendar.getMonthLength(date.year, date.month)).map { day ->
        calendar.createDate(date.year, date.month, day)
    }
