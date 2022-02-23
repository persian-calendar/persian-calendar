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

    // State
    private val _time = MutableStateFlow(System.currentTimeMillis())
    private val _displayNightMask = MutableStateFlow(true)
    private val _displayLocation = MutableStateFlow(true)
    private val _displayGrid = MutableStateFlow(false)
    private val _isDirectPathMode = MutableStateFlow(false)
    private val _directPathDestination = MutableStateFlow<Coordinates?>(null)

    // Subscriptions
    val time: StateFlow<Long> = _time
    val displayNightMask: StateFlow<Boolean> = _displayNightMask
    val displayLocation: StateFlow<Boolean> = _displayLocation
    val displayGrid: StateFlow<Boolean> = _displayGrid
    val isDirectPathMode: StateFlow<Boolean> = _isDirectPathMode
    val directPathDestination: StateFlow<Coordinates?> = _directPathDestination
    val updateEvent = merge(
        _time, _displayNightMask, _displayLocation, _displayGrid, _isDirectPathMode,
        _directPathDestination
    ).debounce(10) // just to filter initial immediate emits

    // Commands
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

    fun toggleNightMask() {
        _displayNightMask.value = !_displayNightMask.value
    }

    fun toggleDisplayLocation() {
        _displayLocation.value = !displayLocation.value
    }

    fun turnOnDisplayLocation() {
        _displayLocation.value = true
    }

    fun toggleDisplayGrid() {
        _displayGrid.value = !_displayGrid.value
    }

    fun toggleDirectPathMode() {
        _isDirectPathMode.value = !_isDirectPathMode.value
    }

    fun changeDirectPathDestination(coordinates: Coordinates?) {
        _directPathDestination.value = coordinates
    }
}
