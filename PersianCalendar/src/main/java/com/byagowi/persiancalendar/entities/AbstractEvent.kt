package com.byagowi.persiancalendar.entities

import io.github.persiancalendar.calendar.AbstractDate

abstract class AbstractEvent<T : AbstractDate> {
    lateinit var date: T
    lateinit var title: String
    var isHoliday: Boolean = false

    override fun toString(): String = title
}
