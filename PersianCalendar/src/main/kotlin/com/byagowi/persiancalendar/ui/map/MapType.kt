package com.byagowi.persiancalendar.ui.map

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class MapType(@get:StringRes val title: Int, val isCrescentVisibility: Boolean = false) {
    NONE(R.string.none),
    DAY_NIGHT(R.string.show_night_mask_label),
    MOON_VISIBILITY(R.string.moon_visibility),
    MAGNETIC_FIELD_STRENGTH(R.string.magnetic_field_strength),
    MAGNETIC_DECLINATION(R.string.magnetic_declination),
    MAGNETIC_INCLINATION(R.string.magnetic_inclination),
    TIME_ZONES(R.string.time_zones_en),
    TECTONIC_PLATES(R.string.tectonic_plates),
    EVENING_YALLOP(R.string.crescent_evening_visibility_yallop, isCrescentVisibility = true),
    EVENING_ODEH(R.string.crescent_evening_visibility_odeh, isCrescentVisibility = true),
    MORNING_YALLOP(R.string.crescent_morning_visibility_yallop, isCrescentVisibility = true),
    MORNING_ODEH(R.string.crescent_morning_visibility_odeh, isCrescentVisibility = true)
}
