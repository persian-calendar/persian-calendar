package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.friday
import com.byagowi.persiancalendar.shared.generated.resources.friday_short
import com.byagowi.persiancalendar.shared.generated.resources.monday
import com.byagowi.persiancalendar.shared.generated.resources.monday_short
import com.byagowi.persiancalendar.shared.generated.resources.saturday
import com.byagowi.persiancalendar.shared.generated.resources.saturday_short
import com.byagowi.persiancalendar.shared.generated.resources.sunday
import com.byagowi.persiancalendar.shared.generated.resources.sunday_short
import com.byagowi.persiancalendar.shared.generated.resources.thursday
import com.byagowi.persiancalendar.shared.generated.resources.thursday_short
import com.byagowi.persiancalendar.shared.generated.resources.tuesday
import com.byagowi.persiancalendar.shared.generated.resources.tuesday_short
import com.byagowi.persiancalendar.shared.generated.resources.wednesday
import com.byagowi.persiancalendar.shared.generated.resources.wednesday_short

// Order of this enum is a legacy for this codebase, and it
// *DIFFERS* from ISO-8601 standard and isn't 1 (Monday) to 7 (Sunday), unfortunately
enum class WeekDay {
    SATURDAY, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY;

    operator fun minus(other: WeekDay): Int = (ordinal + 7 - other.ordinal) % 7
    operator fun plus(other: Int): WeekDay = entries[(ordinal + other) % 7]

    companion object {
        // Get a WeekDay from ISO-8601's ordinal, from 1 (Monday) to 7 (Sunday).
        fun fromISO8601(value: Int): WeekDay = entries[value % 7]
    }
}
