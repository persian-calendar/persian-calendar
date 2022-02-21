package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ConverterViewModel : ViewModel() {

    private val _calendar = MutableStateFlow(mainCalendar)
    val calendar: StateFlow<CalendarType> = _calendar

    private val _selectedDate = MutableStateFlow(Jdn.today())
    val selectedDate: StateFlow<Jdn> = _selectedDate

    private val _secondSelectedDate = MutableStateFlow(Jdn.today())
    val secondSelectedDate: StateFlow<Jdn> = _secondSelectedDate

    private val _isDayDistance = MutableStateFlow(false)
    val isDayDistance: StateFlow<Boolean> = _isDayDistance

    // Events
    val todayButtonVisibilityEvent = merge(selectedDate, secondSelectedDate, isDayDistance).map {
        val todayJdn = Jdn.today()
        selectedDate.value != todayJdn ||
                (isDayDistance.value && secondSelectedDate.value != todayJdn)
    }

    val updateEvent = merge(calendar, selectedDate, secondSelectedDate, isDayDistance)

    // Commands
    fun changeCalendar(calendarType: CalendarType) {
        _calendar.value = calendarType
    }

    fun changeSelectedDate(jdn: Jdn) {
        _selectedDate.value = jdn
    }

    fun changeSecondSelectedDate(jdn: Jdn) {
        _secondSelectedDate.value = jdn
    }

    fun changeIsDayDistance(value: Boolean) {
        _isDayDistance.value = value
    }
}
