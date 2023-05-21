package com.byagowi.persiancalendar.ui.map

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class MapType(@StringRes val title: Int, val isCrescentVisibility: Boolean = false) {
    None(R.string.none),
    DayNight(R.string.show_night_mask_label),
    MoonVisibility(R.string.moon_visibility),
    MagneticFieldStrength(R.string.magnetic_field_strength),
    MagneticDeclination(R.string.magnetic_declination),
    MagneticInclination(R.string.magnetic_inclination),
    TimeZones(R.string.time_zones_en),
    TectonicPlates(R.string.tectonic_plates),
    Yallop(R.string.crescent_visibility_yallop, isCrescentVisibility = true),
    Odeh(R.string.crescent_visibility_odeh, isCrescentVisibility = true)
}
