package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.cosinekitty.astronomy.Body

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

    // A concept in planetary hours, see PlanetaryHours.kt, note how similar they are btw
    // Not defined in the entry so it can be lazy loaded as previous issues we have with Body enum
    val ruledBy
        get() = when (this) {
            SATURDAY -> Body.Saturn
            SUNDAY -> Body.Sun
            MONDAY -> Body.Moon
            TUESDAY -> Body.Mars
            WEDNESDAY -> Body.Mercury
            THURSDAY -> Body.Jupiter
            FRIDAY -> Body.Venus
        }
}
