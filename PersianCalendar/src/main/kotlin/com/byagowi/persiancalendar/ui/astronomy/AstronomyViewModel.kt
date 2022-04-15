package com.byagowi.persiancalendar.ui.astronomy

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AstronomyViewModel : ViewModel() {
    private val _isTropical = MutableStateFlow(false)
    val isTropical: StateFlow<Boolean> = _isTropical

    private val _time = MutableStateFlow(DEFAULT_TIME)
    val time: StateFlow<Int> = _time // is offset in minutes

    private val _mode = MutableStateFlow(Mode.Earth)
    val mode: StateFlow<Mode> = _mode

    // Events
    val resetButtonVisibilityEvent = time
        .map { it != 0 }
        .distinctUntilChanged()

    // Commands
    fun changeTime(value: Int) {
        _time.value = value
    }

    fun addTime(offset: Int) {
        _time.value += offset
    }

    fun changeToDayOffset(dayOffset: Int) {
        _time.value = dayOffset * MINUTES_IN_DAY
    }

    fun addDayOffset(dayOffset: Int) {
        _time.value += dayOffset * MINUTES_IN_DAY
    }

    fun changeTropical(value: Boolean) {
        _isTropical.value = value
    }

    fun changeScreenMode(value: Mode) {
        _mode.value = value
    }

    enum class Mode { Earth, Moon }

    companion object {
        const val DEFAULT_TIME = Int.MIN_VALUE
        private const val MINUTES_IN_DAY = 60 * 24
    }
}
