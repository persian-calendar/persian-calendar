package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.MutableStateFlow

class ConverterViewModel : ViewModel() {
    val calendarType = MutableStateFlow(mainCalendar)
    val jdn = MutableStateFlow(Jdn.today())
    val distanceJdn = MutableStateFlow(Jdn.today())
    val isDayDistance = MutableStateFlow(false)
    val todayButtonVisibility = MutableStateFlow(false)
}
