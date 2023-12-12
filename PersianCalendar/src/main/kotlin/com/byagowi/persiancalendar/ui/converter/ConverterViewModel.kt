package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import java.util.GregorianCalendar
import java.util.TimeZone

class ConverterViewModel : ViewModel() {
    private val _calendar = MutableStateFlow(mainCalendar)
    val calendar: StateFlow<CalendarType> get() = _calendar

    private val _selectedDate = MutableStateFlow(Jdn.today())
    val selectedDate: StateFlow<Jdn> get() = _selectedDate

    private val _secondSelectedDate = MutableStateFlow(Jdn.today())
    val secondSelectedDate: StateFlow<Jdn> get() = _secondSelectedDate

    private val _screenMode = MutableStateFlow(ConverterScreenMode.entries[0])
    val screenMode: StateFlow<ConverterScreenMode> get() = _screenMode

    private val _calculatorInputText = MutableStateFlow("1d 2h 3m 4s + 4h 5s - 2030s + 28h")
    val calculatorInputText: StateFlow<String> get() = _calculatorInputText

    private val _qrCodeInputText = MutableStateFlow("https://example.com")
    val qrCodeInputText: StateFlow<String> get() = _qrCodeInputText

    private val _firstTimeZone = MutableStateFlow(TimeZone.getDefault())
    val firstTimeZone: StateFlow<TimeZone> get() = _firstTimeZone

    private val _secondTimeZone = MutableStateFlow(utc)
    val secondTimeZone: StateFlow<TimeZone> get() = _secondTimeZone

    private val _clock = MutableStateFlow(GregorianCalendar())
    val clock: StateFlow<GregorianCalendar> get() = _clock

    // Events
    val todayButtonVisibilityEvent = merge(
        _selectedDate, _secondSelectedDate, _screenMode, _clock
    ).map {
        when (screenMode.value) {
            ConverterScreenMode.Calculator, ConverterScreenMode.QrCode -> false

            ConverterScreenMode.Converter -> selectedDate.value != Jdn.today()

            ConverterScreenMode.Distance -> {
                val today = Jdn.today()
                selectedDate.value != today || secondSelectedDate.value != today
            }

            ConverterScreenMode.TimeZones -> {
                !haveSameClock(clock.value, GregorianCalendar(clock.value.timeZone))
                        || firstTimeZone.value != TimeZone.getDefault()
                        || secondTimeZone.value != utc
            }
        }
    }

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
        _calculatorInputText.value = text
    }

    fun changeQrCodeInput(text: String) {
        _qrCodeInputText.value = text
    }

    fun changeClock(hourOfDay: Int, minute: Int, timeZone: TimeZone) {
        val newClock = GregorianCalendar(timeZone)
        newClock[GregorianCalendar.HOUR_OF_DAY] = hourOfDay
        newClock[GregorianCalendar.MINUTE] = minute
        if (newClock.timeInMillis != _clock.value.timeInMillis) _clock.value = newClock
    }

    fun changeFirstTimeZone(timeZone: TimeZone) {
        _firstTimeZone.value = timeZone
    }

    fun changeSecondTimeZone(timeZone: TimeZone) {
        _secondTimeZone.value = timeZone
    }

    fun resetTimeZoneClock() {
        _firstTimeZone.value = TimeZone.getDefault()
        _secondTimeZone.value = utc
        _clock.value = GregorianCalendar()
    }

    companion object {
        private val utc = TimeZone.getTimeZone("UTC")
        private fun haveSameClock(first: GregorianCalendar, second: GregorianCalendar): Boolean {
            return first[GregorianCalendar.MINUTE] == second[GregorianCalendar.MINUTE] &&
                    first[GregorianCalendar.HOUR_OF_DAY] == second[GregorianCalendar.HOUR_OF_DAY]
        }
    }
}
