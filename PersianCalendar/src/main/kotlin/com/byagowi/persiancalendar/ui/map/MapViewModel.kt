package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.ONE_HOUR_IN_MILLIS
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    // Values
    val time: Long get() = _time.value
    val displayNightMask: Boolean get() = _displayNightMask.value
    val displayLocation: Boolean get() = _displayLocation.value
    val displayGrid: Boolean get() = _displayGrid.value
    val isDirectPathMode: Boolean get() = _isDirectPathMode.value
    val directPathDestination: Coordinates? get() = _directPathDestination.value

    // Events
    val updateEvent: Flow<*> = merge(
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
        _displayNightMask.value = !displayNightMask
    }

    fun toggleDisplayLocation() {
        _displayLocation.value = !displayLocation
    }

    fun turnOnDisplayLocation() {
        _displayLocation.value = true
    }

    fun toggleDisplayGrid() {
        _displayGrid.value = !displayGrid
    }

    fun toggleDirectPathMode() {
        _isDirectPathMode.value = !isDirectPathMode
    }

    fun changeDirectPathDestination(coordinates: Coordinates?) {
        _directPathDestination.value = coordinates
    }
}
