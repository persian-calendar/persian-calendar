package com.byagowi.persiancalendar.ui.map

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.praytimes.Coordinates

data class MapState(
    val time: Long = System.currentTimeMillis(),
    val maskType: MaskType = MaskType.DayNight,
    val displayLocation: Boolean = true,
    val displayGrid: Boolean = false,
    val isDirectPathMode: Boolean = false,
    val directPathDestination: Coordinates? = null
)

enum class MaskType(@StringRes val title: Int) {
    None(R.string.none),
    DayNight(R.string.show_night_mask_label),
    MagneticFieldStrength(R.string.magnetic_field_strength),
    MagneticDeclination(R.string.magnetic_declination),
    MagneticInclination(R.string.magnetic_inclination)
}
