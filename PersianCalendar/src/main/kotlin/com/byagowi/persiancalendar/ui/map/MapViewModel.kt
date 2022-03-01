package com.byagowi.persiancalendar.ui.map

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.ONE_HOUR_IN_MILLIS
import com.byagowi.persiancalendar.utils.setState
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {
    private val _state = MutableStateFlow(MapState.initial)
    val state: StateFlow<MapState> get() = _state

    // Commands
    fun subtractOneHour() {
        _state.setState { copy(time = time - ONE_HOUR_IN_MILLIS) }
    }

    fun addOneHour() {
        _state.setState { copy(time = time + ONE_HOUR_IN_MILLIS) }
    }

    fun subtractOneDay() {
        _state.setState { copy(time = time - DAY_IN_MILLIS) }
    }

    fun addOneDay() {
        _state.setState { copy(time = time + DAY_IN_MILLIS) }
    }

    fun toggleNightMask() {
        _state.setState { copy(displayNightMask = displayNightMask.not()) }
    }

    fun toggleDisplayLocation() {
        _state.setState { copy(displayLocation = displayLocation.not()) }
    }

    fun turnOnDisplayLocation() {
        _state.setState { copy(displayLocation = true) }
    }

    fun toggleDisplayGrid() {
        _state.setState { copy(displayGrid = displayGrid.not()) }
    }

    fun toggleDirectPathMode() {
        _state.setState { copy(isDirectPathMode = isDirectPathMode.not()) }
    }

    fun changeDirectPathDestination(coordinates: Coordinates?) {
        _state.setState { copy(directPathDestination = coordinates) }
    }
}
