package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow

class MapViewModel : ViewModel() {
    val time = MutableStateFlow(System.currentTimeMillis())
    val displayNightMask = MutableStateFlow(true)
    val displayLocation = MutableStateFlow(true)
    val displayGrid = MutableStateFlow(false)
    val isDirectPathMode = MutableStateFlow(false)
    val toCoordinates = MutableStateFlow<Coordinates?>(null)
}
