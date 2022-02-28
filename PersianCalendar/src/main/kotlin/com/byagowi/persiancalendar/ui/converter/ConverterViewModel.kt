package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ConverterViewModel : ViewModel() {

    // State
    private val _calendar = MutableStateFlow(mainCalendar)
    private val _selectedDate = MutableStateFlow(Jdn.today())
    private val _secondSelectedDate = MutableStateFlow(Jdn.today())
    private val _screenMode = MutableStateFlow(ConverterScreenMode.Converter)

    // Values
    val calendar: CalendarType get() = _calendar.value
    val selectedDate: Jdn get() = _selectedDate.value
    val secondSelectedDate: Jdn get() = _secondSelectedDate.value
    val screenMode: ConverterScreenMode get() = _screenMode.value

    // Events
    val calendarChangeEvent: Flow<CalendarType> get() = _calendar
    val screenModeChangeEvent: Flow<ConverterScreenMode> get() = _screenMode
    val todayButtonVisibilityEvent = merge(_selectedDate, _secondSelectedDate, _screenMode).map {
        val todayJdn = Jdn.today()
        selectedDate != todayJdn ||
                (screenMode == ConverterScreenMode.Distance && secondSelectedDate != todayJdn)
    }
    val updateEvent = merge(_calendar, _selectedDate, _secondSelectedDate, _screenMode)

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

    fun changeScreenMode(value: ConverterScreenMode) {
        _screenMode.value = value
    }
}
