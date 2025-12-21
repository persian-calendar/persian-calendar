package com.byagowi.persiancalendar.ui.calendar.shiftwork

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord

class ShiftWorkViewModel : ViewModel() {
    var startingDate by mutableStateOf(Jdn.today())
    var recurs by mutableStateOf(true)
    var isFirstSetup by mutableStateOf(true)

    private var _shiftWorks = mutableStateOf(emptyList<ShiftWorkRecord>())
    val shiftWorks by _shiftWorks

    fun changeShiftWorks(value: List<ShiftWorkRecord>) {
        _shiftWorks.value = value
    }

    fun changeShiftWorkTypeOfPosition(position: Int, type: String) {
        _shiftWorks.value = shiftWorks.mapIndexed { i, (currentType, length) ->
            ShiftWorkRecord(if (position == i) type else currentType, length)
        }
    }

    fun changeShiftWorkLengthOfPosition(position: Int, length: Int) {
        _shiftWorks.value = shiftWorks.mapIndexed { i, (type, currentLength) ->
            ShiftWorkRecord(type, if (position == i) length else currentLength)
        }
    }

    fun removeShiftWorkPosition(position: Int) {
        _shiftWorks.value = shiftWorks.filterIndexed { i, _ -> i != position }
    }
}
