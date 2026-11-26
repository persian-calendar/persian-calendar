package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.utils.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate

fun eventCalendar(event: Map<String, Any>): Calendar? = when (event["calendar"]) {
    "Gregorian" -> Calendar.GREGORIAN
    "Persian" -> Calendar.SHAMSI
    "Hijri" -> Calendar.ISLAMIC
    "Nepali" -> Calendar.NEPALI
    else -> null
}

fun getDateInstance(event: Map<String, Any>, year: Int, type: Calendar): AbstractDate? {
    return when (event["rule"]) {
        "single event" -> {
            if ((event["year"] as? Int).debugAssertNotNull != year) return null
            val month = (event["month"] as? Int).debugAssertNotNull ?: return null
            val day = (event["day"] as? Int).debugAssertNotNull ?: return null
            type.createDate(year, month, day)
        }

        "nth day from" -> {
            val nth = (event["nth"] as? Int).debugAssertNotNull ?: return null
            val day = (event["day"] as? Int).debugAssertNotNull ?: return null
            val month = (event["month"] as? Int).debugAssertNotNull ?: return null
            (Jdn(type, year, month, day) + nth - 1) on type
        }

        "end of month" -> {
            val month = (event["month"] as? Int).debugAssertNotNull ?: return null
            type.createDate(year, month, type.getMonthLength(year, month))
        }

        "last weekday of month" -> {
            val month = (event["month"] as? Int).debugAssertNotNull ?: return null
            val weekDay = (event["weekday"] as? Int).debugAssertNotNull ?: return null
            val offset = (event["offset"] as? Int) ?: 0
            type.createDate(year, month, type.getLastWeekDayOfMonth(year, month, weekDay) + offset)
        }

        "nth weekday of month" -> {
            val month = (event["month"] as? Int).debugAssertNotNull ?: return null
            val weekDay = (event["weekday"] as? Int).debugAssertNotNull ?: return null
            val nth = (event["nth"] as? Int).debugAssertNotNull ?: return null
            type.createDate(year, month, type.getNthWeekDayOfMonth(year, month, weekDay, nth))
        }

        else -> null.debugAssertNotNull
    }
}
