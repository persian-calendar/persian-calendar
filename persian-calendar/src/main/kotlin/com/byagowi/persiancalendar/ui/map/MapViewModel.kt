package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.ONE_HOUR_IN_MILLIS
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class MapViewModel : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> get() = _state

    // Commands
    fun subtractOneHour() {
        _state.update { it.copy(time = it.time - ONE_HOUR_IN_MILLIS) }
    }

    fun addOneHour() {
        _state.update { it.copy(time = it.time + ONE_HOUR_IN_MILLIS) }
    }

    fun addOneMinute() {
        _state.update { it.copy(time = it.time + ONE_MINUTE_IN_MILLIS) }
    }

    fun addDays(days: Int) {
        _state.update { it.copy(time = it.time + DAY_IN_MILLIS * days) }
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
