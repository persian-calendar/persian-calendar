package com.byagowi.persiancalendar.ui.map

import io.github.persiancalendar.praytimes.Coordinates

data class MapState(
    val time: Long = System.currentTimeMillis(),
    val mapType: MapType = MapType.DayNight,
    val displayLocation: Boolean = true,
    val displayGrid: Boolean = false,
    val isDirectPathMode: Boolean = false,
    val coordinates: Coordinates? = null,
    val directPathDestination: Coordinates? = null
)
