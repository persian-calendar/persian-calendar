package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

// Order of week day is a legacy for this code base
// *DIFFERS* from ISO-8601 standard, 1 (Monday) to 7 (Sunday), unfortunately
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
}
