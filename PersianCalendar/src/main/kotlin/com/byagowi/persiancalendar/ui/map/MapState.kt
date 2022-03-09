package com.byagowi.persiancalendar.ui.map

import io.github.persiancalendar.praytimes.Coordinates

data class MapState(
    val time: Long = System.currentTimeMillis(),
    val displayNightMask: Boolean = true,
    val displayLocation: Boolean = true,
    val displayGrid: Boolean = false,
    val isDirectPathMode: Boolean = false,
    val directPathDestination: Coordinates? = null
)
