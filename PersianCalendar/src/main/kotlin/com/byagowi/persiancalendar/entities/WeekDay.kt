package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.cosinekitty.astronomy.Body

// Order of week day is a legacy for this code base
// *DIFFERS* from ISO-8601 standard, 1 (Monday) to 7 (Sunday), unfortunately
enum class WeekDay(
    // A concept in planetary hours, see PlanetaryHours.kt, note how similar they are btw
    val ruledBy: Body,
    @param:StringRes val titleId: Int,
    @param:StringRes val shortTitleId: Int,
) {
    SATURDAY(Body.Saturn, R.string.saturday, R.string.saturday_short),
    SUNDAY(Body.Sun, R.string.sunday, R.string.sunday_short),
    MONDAY(Body.Moon, R.string.monday, R.string.monday_short),
    TUESDAY(Body.Mars, R.string.tuesday, R.string.tuesday_short),
    WEDNESDAY(Body.Mercury, R.string.wednesday, R.string.wednesday_short),
    THURSDAY(Body.Jupiter, R.string.thursday, R.string.thursday_short),
    FRIDAY(Body.Venus, R.string.friday, R.string.friday_short)
}
