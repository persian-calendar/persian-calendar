package com.byagowi.persiancalendar.ui.map

import io.github.persiancalendar.praytimes.Coordinates

data class MapState(
    val time: Long = System.currentTimeMillis(),
    val maskType: MaskType = MaskType.DayNight,
    val displayLocation: Boolean = true,
    val displayGrid: Boolean = false,
    val isDirectPathMode: Boolean = false,
    val directPathDestination: Coordinates? = null
)

enum class MaskType(val title: String) {
    None("None"),
    DayNight("Day-night map"),
    MagneticFieldStrength("Magnetic field strength"),
    MagneticDeclination("Magnetic declination"),
    MagneticInclination("Magnetic inclination")
}
