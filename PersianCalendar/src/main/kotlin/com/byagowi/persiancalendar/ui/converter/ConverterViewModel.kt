package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ConverterViewModel : ViewModel() {
    val calendarType = MutableStateFlow(mainCalendar)
    val selectedDate = MutableStateFlow(Jdn.today())
    val secondSelectedDate = MutableStateFlow(Jdn.today())
    val isDayDistance = MutableStateFlow(false)
    val todayButtonVisibility = merge(selectedDate, secondSelectedDate, isDayDistance).map {
        val todayJdn = Jdn.today()
        selectedDate.value != todayJdn ||
                (isDayDistance.value && secondSelectedDate.value != todayJdn)
    }
    val updateResult = merge(calendarType, selectedDate, secondSelectedDate, isDayDistance)
}
