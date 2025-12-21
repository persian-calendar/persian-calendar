package com.byagowi.persiancalendar.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.github.persiancalendar.praytimes.Coordinates
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class MapViewModel : ViewModel() {
    private val _state = mutableStateOf(MapState())
    val state by _state

    // Commands
    fun subtractOneHour() {
        _state.value = state.copy(time = state.time - 1.hours.inWholeMilliseconds)
    }

    fun addOneHour() {
        _state.value = state.copy(time = state.time + 1.hours.inWholeMilliseconds)
    }

    fun addOneMinute() {
        _state.value = state.copy(time = state.time + 1.minutes.inWholeMilliseconds)
    }

    fun addDays(days: Int) {
        _state.value = state.copy(time = state.time + days.days.inWholeMilliseconds)
    }

    fun changeToTime(time: Date) {
        _state.value = state.copy(time = time.time)
    }

    fun changeMapType(mapType: MapType) {
        _state.value = state.copy(mapType = mapType)
    }

    fun toggleDisplayLocation() {
        _state.value = state.copy(displayLocation = !state.displayLocation)
    }

    fun toggleDisplayGrid() {
        _state.value = state.copy(displayGrid = !state.displayGrid)
    }

    fun toggleDirectPathMode() {
        _state.value = state.copy(
            isDirectPathMode = !state.isDirectPathMode,
            directPathDestination = state.directPathDestination.takeIf { _ -> !state.isDirectPathMode },
        )
    }

    fun changeCurrentCoordinates(coordinates: Coordinates?) {
        _state.value = state.copy(coordinates = coordinates, displayLocation = coordinates != null)
    }

    fun changeDirectPathDestination(coordinates: Coordinates?) {
        _state.value = state.copy(directPathDestination = coordinates)
    }
}
