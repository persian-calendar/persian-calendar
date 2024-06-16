package com.byagowi.persiancalendar.ui.map

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class MapType(@StringRes val title: Int, val isCrescentVisibility: Boolean = false) {
    NONE(R.string.none),
    DAY_NIGHT(R.string.show_night_mask_label),
    MOON_VISIBILITY(R.string.moon_visibility),
    MAGNETIC_FIELD_STRENGTH(R.string.magnetic_field_strength),
    MAGNETIC_DECLINATION(R.string.magnetic_declination),
    MAGNETIC_INCLINATION(R.string.magnetic_inclination),
    TIME_ZONES(R.string.time_zones_en),
    TECTONIC_PLATES(R.string.tectonic_plates),
    YALLOP(R.string.crescent_visibility_yallop, isCrescentVisibility = true),
    ODEH(R.string.crescent_visibility_odeh, isCrescentVisibility = true)
}
