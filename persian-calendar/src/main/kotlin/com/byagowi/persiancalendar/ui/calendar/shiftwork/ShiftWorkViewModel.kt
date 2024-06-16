package com.byagowi.persiancalendar.ui.calendar.shiftwork

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ShiftWorkViewModel : ViewModel() {
    private val _shiftWorks = MutableStateFlow(emptyList<ShiftWorkRecord>())
    val shiftWorks: StateFlow<List<ShiftWorkRecord>> get() = _shiftWorks

    private val _startingDate = MutableStateFlow(Jdn.today())
    val startingDate: StateFlow<Jdn> get() = _startingDate

    private val _recurs = MutableStateFlow(true)
    val recurs: StateFlow<Boolean> get() = _recurs

    private val _isFirstSetup = MutableStateFlow(true)
    val isFirstSetup: StateFlow<Boolean> get() = _isFirstSetup

    // Commands
    fun changeShiftWorks(value: List<ShiftWorkRecord>) {
        _shiftWorks.value = value
    }

    fun changeShiftWorkTypeOfPosition(position: Int, type: String) {
        _shiftWorks.value = shiftWorks.value.mapIndexed { i, (currentType, length) ->
            ShiftWorkRecord(if (position == i) type else currentType, length)
        }
    }

    fun changeShiftWorkLengthOfPosition(position: Int, length: Int) {
        _shiftWorks.value = shiftWorks.value.mapIndexed { i, (type, currentLength) ->
            ShiftWorkRecord(type, if (position == i) length else currentLength)
        }
    }

    fun removeShiftWorkPosition(position: Int) {
        _shiftWorks.value = shiftWorks.value.filterIndexed { i, _ -> i != position }
    }

    fun changeStartingDate(value: Jdn) {
        _startingDate.value = value
    }

    fun changeRecurs(value: Boolean) {
        _recurs.value = value
    }

    fun changeIsFirstSetup(value: Boolean) {
        _isFirstSetup.value = value
    }
}
