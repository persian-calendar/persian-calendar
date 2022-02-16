package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.MutableStateFlow

class ConverterViewModel : ViewModel() {
    val calendarType by lazy { MutableStateFlow(mainCalendar) }
    val jdn by lazy { MutableStateFlow(Jdn.today()) }
    val distanceJdn by lazy { MutableStateFlow(Jdn.today()) }
    val isDayDistance by lazy { MutableStateFlow(false) }
    val todayButtonVisibility by lazy { MutableStateFlow(false) }
}
