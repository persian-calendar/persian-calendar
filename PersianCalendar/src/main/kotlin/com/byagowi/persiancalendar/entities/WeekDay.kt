package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.global.weekDaysInitials

// Order of this enum is a legacy for this codebase and it
// *DIFFERS* from ISO-8601 standard and isn't 1 (Monday) to 7 (Sunday), unfortunately
enum class WeekDay(
    @param:StringRes val titleId: Int,
    @param:StringRes val shortTitleId: Int,
) {
    SATURDAY(R.string.saturday, R.string.saturday_short),
    SUNDAY(R.string.sunday, R.string.sunday_short),
    MONDAY(R.string.monday, R.string.monday_short),
    TUESDAY(R.string.tuesday, R.string.tuesday_short),
    WEDNESDAY(R.string.wednesday, R.string.wednesday_short),
    THURSDAY(R.string.thursday, R.string.thursday_short),
    FRIDAY(R.string.friday, R.string.friday_short);

    operator fun minus(other: WeekDay): Int = (ordinal + 7 - other.ordinal) % 7
    operator fun plus(other: Int): Int = (ordinal + other) % 7

    // Better to use titleId and shortTitleId but it's ok to not to in widgets and so
    val title: String get() = weekDays[this.ordinal]
    val shortTitle get() = weekDaysInitials[this.ordinal]
}
