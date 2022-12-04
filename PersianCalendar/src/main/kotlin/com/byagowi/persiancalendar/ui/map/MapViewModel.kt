package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.ONE_HOUR_IN_MILLIS
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.*

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

    fun subtractOneDay() {
        _state.update { it.copy(time = it.time - DAY_IN_MILLIS) }
    }

    fun addOneDay() {
        _state.update { it.copy(time = it.time + DAY_IN_MILLIS) }
    }

    fun subtractTenDays() {
        _state.update { it.copy(time = it.time - DAY_IN_MILLIS * 10) }
    }

    fun addTenDays() {
        _state.update { it.copy(time = it.time + DAY_IN_MILLIS * 10) }
    }

    fun changeToTime(time: Date) {
        _state.update { it.copy(time = time.time) }
    }

    fun changemapType(mapType: MapType) {
        _state.update { it.copy(mapType = mapType) }
    }

    fun toggleDisplayLocation() {
        _state.update { it.copy(displayLocation = !it.displayLocation) }
    }

    fun turnOnDisplayLocation() {
        _state.update { it.copy(displayLocation = true) }
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

    fun changeDirectPathDestination(coordinates: Coordinates?) {
        _state.update { it.copy(directPathDestination = coordinates) }
    }
}
