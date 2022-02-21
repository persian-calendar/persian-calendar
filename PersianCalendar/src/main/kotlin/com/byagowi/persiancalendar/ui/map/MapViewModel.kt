package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.ONE_HOUR_IN_MILLIS
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.merge

class MapViewModel : ViewModel() {

    private val _time = MutableStateFlow(System.currentTimeMillis())
    val time: StateFlow<Long> = _time

    fun subtractOneHour() {
        _time.value -= ONE_HOUR_IN_MILLIS
    }

    fun addOneHour() {
        _time.value += ONE_HOUR_IN_MILLIS
    }

    fun subtractOneDay() {
        _time.value -= DAY_IN_MILLIS
    }

    fun addOneDay() {
        _time.value += DAY_IN_MILLIS
    }

    private val _displayNightMask = MutableStateFlow(true)
    val displayNightMask: StateFlow<Boolean> = _displayNightMask
    fun toggleNightMask() {
        _displayNightMask.value = !_displayNightMask.value
    }

    private val _displayLocation = MutableStateFlow(true)
    val displayLocation: StateFlow<Boolean> = _displayLocation
    fun toggleDisplayLocation() {
        _displayLocation.value = !displayLocation.value
    }

    fun turnOnDisplayLocation() {
        _displayLocation.value = true
    }

    private val _displayGrid = MutableStateFlow(false)
    val displayGrid: StateFlow<Boolean> = _displayGrid
    fun toggleDisplayGrid() {
        _displayGrid.value = !_displayGrid.value
    }

    private val _isDirectPathMode = MutableStateFlow(false)
    val isDirectPathMode: StateFlow<Boolean> = _isDirectPathMode
    fun toggleDirectPathMode() {
        _isDirectPathMode.value = !_isDirectPathMode.value
    }

    // the destination direct path mode will draw the shortest real path to
    private val _directPathDestination = MutableStateFlow<Coordinates?>(null)
    val directPathDestination: StateFlow<Coordinates?> = _directPathDestination
    fun changeDirectPathDestination(coordinates: Coordinates?) {
        _directPathDestination.value = coordinates
    }

    val updateEvent = merge(
        _time, _displayNightMask, _displayLocation, _displayGrid, _isDirectPathMode,
        _directPathDestination
    ).debounce(10) // just to filter initial immediate emits
}
