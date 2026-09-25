package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.global.weekDaysTitles
import com.byagowi.persiancalendar.global.weekDaysTitlesInitials

val WeekDay.shortTitle: String get() = weekDaysTitlesInitials[this.ordinal]
val WeekDay.title: String get() = weekDaysTitles[this.ordinal]
