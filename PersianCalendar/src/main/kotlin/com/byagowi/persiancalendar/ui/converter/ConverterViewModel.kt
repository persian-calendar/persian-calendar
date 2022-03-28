package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ConverterViewModel : ViewModel() {
    private val _calendar = MutableStateFlow(mainCalendar)
    val calendar: StateFlow<CalendarType> get() = _calendar

    private val _selectedDate = MutableStateFlow(Jdn.today())
    val selectedDate: StateFlow<Jdn> get() = _selectedDate

    private val _secondSelectedDate = MutableStateFlow(Jdn.today())
    val secondSelectedDate: StateFlow<Jdn> get() = _secondSelectedDate

    private val _screenMode = MutableStateFlow(ConverterScreenMode.Converter)
    val screenMode: StateFlow<ConverterScreenMode> get() = _screenMode

    private val _inputText = MutableStateFlow("1d 2h 3m 4s + 4h 5s - 2030s + 28h")
    val inputText: StateFlow<String> get() = _inputText

    // Events
    val calendarChangeEvent: Flow<CalendarType> get() = _calendar
    val screenModeChangeEvent: Flow<ConverterScreenMode> get() = _screenMode
    val todayButtonVisibilityEvent = merge(_selectedDate, _secondSelectedDate, _screenMode).map {
        val today = Jdn.today()
        when (screenMode.value) {
            ConverterScreenMode.Converter -> selectedDate.value != today
            ConverterScreenMode.Distance ->
                selectedDate.value != today || secondSelectedDate.value != today
            ConverterScreenMode.Calculator -> false
        }
    }
    val updateEvent = merge(_calendar, _selectedDate, _secondSelectedDate, _screenMode, _inputText)

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

    fun changeCalculatorInput(text: String) {
        _inputText.value = text
    }
}
