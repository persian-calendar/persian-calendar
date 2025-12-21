package com.byagowi.persiancalendar.ui.calendar.shiftwork

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord

class ShiftWorkViewModel : ViewModel() {
    var startingDate by mutableStateOf(Jdn.today())
    var recurs by mutableStateOf(true)
    var isFirstSetup by mutableStateOf(true)
    var shiftWorks = mutableStateListOf<ShiftWorkRecord>()

    fun updateItem(position: Int, function: (ShiftWorkRecord) -> ShiftWorkRecord) {
        shiftWorks[position] = function(shiftWorks[position])
    }
}
