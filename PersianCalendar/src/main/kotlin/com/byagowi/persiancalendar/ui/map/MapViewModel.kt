package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.utils.HALF_SECOND_IN_MILLIS
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.merge

class MapViewModel : ViewModel() {
    val time = MutableStateFlow(System.currentTimeMillis())
    val displayNightMask = MutableStateFlow(true)
    val displayLocation = MutableStateFlow(true)
    val displayGrid = MutableStateFlow(false)
    val isDirectPathMode = MutableStateFlow(false)
    val toCoordinates = MutableStateFlow<Coordinates?>(null)
    val updateMap = merge(
        time, displayNightMask, displayLocation, displayGrid, isDirectPathMode, toCoordinates
    ).debounce(10) // just to filter initial immediate emits
}
