package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.utils.CalendarType

class CalendarTypeItem(val type: CalendarType, private val title: String) {

    override fun toString(): String = title
}