package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class MapViewModel : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> get() = _state

    // Commands
    fun subtractOneHour() {
        _state.update { it.copy(time = it.time - 1.hours.inWholeMilliseconds) }
    }

    fun addOneHour() {
        _state.update { it.copy(time = it.time + 1.hours.inWholeMilliseconds) }
    }

    fun addOneMinute() {
        _state.update { it.copy(time = it.time + 1.minutes.inWholeMilliseconds) }
    }

    fun addDays(days: Int) {
        _state.update { it.copy(time = it.time + days.days.inWholeMilliseconds) }
    }

    fun changeToTime(time: Date) {
        _state.update { it.copy(time = time.time) }
    }

    fun changeMapType(mapType: MapType) {
        _state.update { it.copy(mapType = mapType) }
    }

    fun toggleDisplayLocation() {
        _state.update { it.copy(displayLocation = !it.displayLocation) }
    }

    fun toggleDisplayGrid() {
        _state.update { it.copy(displayGrid = !it.displayGrid) }
    }

    fun toggleDirectPathMode() {
        _state.update {
            it.copy(
                isDirectPathMode = !it.isDirectPathMode,
                directPathDestination = it.directPathDestination.takeIf { _ -> !it.isDirectPathMode }
            )
        }
    }

    fun changeCurrentCoordinates(coordinates: Coordinates?) {
        _state.update { it.copy(coordinates = coordinates, displayLocation = coordinates != null) }
    }

    fun changeDirectPathDestination(coordinates: Coordinates?) {
        _state.update { it.copy(directPathDestination = coordinates) }
    }
}
