package com.byagowi.persiancalendar.ui.map

import io.github.persiancalendar.praytimes.Coordinates

data class MapState(
    val time: Long,
    val displayNightMask: Boolean,
    val displayLocation: Boolean,
    val displayGrid: Boolean,
    val isDirectPathMode: Boolean,
    val directPathDestination: Coordinates?
) {
    companion object {
        val initial: MapState
            get() = MapState(
                time = System.currentTimeMillis(),
                displayNightMask = true,
                displayLocation = true,
                displayGrid = false,
                isDirectPathMode = false,
                directPathDestination = null
            )
    }
}
