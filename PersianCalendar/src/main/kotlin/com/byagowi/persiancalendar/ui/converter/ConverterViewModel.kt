package com.byagowi.persiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.TimeZone

class ConverterViewModel : ViewModel() {
    private val _calendar = MutableStateFlow(mainCalendar)
    val calendar: StateFlow<Calendar> get() = _calendar

    private val _today = MutableStateFlow(Jdn.today())
    val today: StateFlow<Jdn> get() = _today

    private val _selectedDate = MutableStateFlow(_today.value)
    val selectedDate: StateFlow<Jdn> get() = _selectedDate

    private val _secondSelectedDate = MutableStateFlow(_today.value)
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

    private val _todayButtonVisibility = MutableStateFlow(false)
    val todayButtonVisibility: StateFlow<Boolean> get() = _todayButtonVisibility

    init {
        viewModelScope.launch {
            merge(
                selectedDate, secondSelectedDate, screenMode, today,
                clock, firstTimeZone, secondTimeZone,
            ).collectLatest {
                _todayButtonVisibility.value = when (screenMode.value) {
                    ConverterScreenMode.Calculator, ConverterScreenMode.QrCode -> false

                    ConverterScreenMode.Converter -> selectedDate.value != today.value

                    ConverterScreenMode.Distance -> {
                        selectedDate.value != today.value || secondSelectedDate.value != today.value
                    }

                    ConverterScreenMode.TimeZones -> {
                        !haveSameClock(
                            clock.value,
                            GregorianCalendar(clock.value.timeZone),
                        ) || firstTimeZone.value != TimeZone.getDefault() || secondTimeZone.value != utc
                    }
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(THIRTY_SECONDS_IN_MILLIS)
                val today = Jdn.today()
                if (_today.value != today) _today.value = today
            }
        }
    }

    // Commands
    fun changeCalendar(calendar: Calendar) {
        _calendar.value = calendar
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

    fun changeClock(newClock: GregorianCalendar) {
        _clock.value = newClock
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
