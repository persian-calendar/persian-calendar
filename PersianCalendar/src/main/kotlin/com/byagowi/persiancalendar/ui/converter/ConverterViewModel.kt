package com.byagowi.persiancalendar.ui.converter

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds

class ConverterViewModel : ViewModel() {
    private val _today = mutableStateOf(Jdn.today())
    var today: Jdn by _today

    var calendar by mutableStateOf(mainCalendar)
    var selectedDate by mutableStateOf(today)
    var secondSelectedDate by mutableStateOf(today)
    var screenMode by mutableStateOf(ConverterScreenMode.entries[0])
    var calculatorInputText by mutableStateOf("")
    var qrCodeInputText by mutableStateOf("https://example.com")
    var firstTimeZone: TimeZone by mutableStateOf(TimeZone.getDefault())
    var secondTimeZone: TimeZone by mutableStateOf(utc)
    var clock by mutableStateOf(GregorianCalendar())

    val todayButtonVisibility by derivedStateOf {
        when (screenMode) {
            ConverterScreenMode.CALCULATOR -> calculatorInputText.isNotEmpty()
            ConverterScreenMode.QR_CODE -> false
            ConverterScreenMode.CONVERTER -> selectedDate != today
            ConverterScreenMode.DISTANCE ->
                selectedDate != today || secondSelectedDate != today

            ConverterScreenMode.TIME_ZONES -> {
                !haveSameClock(
                    clock,
                    GregorianCalendar(clock.timeZone),
                ) || firstTimeZone != TimeZone.getDefault() || secondTimeZone != utc
            }
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                delay(30.seconds)
                val today = Jdn.today()
                if (_today.value != today) _today.value = today
            }
        }
    }

    fun resetTimeZoneClock() {
        firstTimeZone = TimeZone.getDefault()
        secondTimeZone = utc
        clock = GregorianCalendar()
    }

    companion object {
        private val utc = TimeZone.getTimeZone("UTC")

        @JvmSynthetic
        private fun haveSameClock(first: GregorianCalendar, second: GregorianCalendar): Boolean {
            return first[GregorianCalendar.MINUTE] == second[GregorianCalendar.MINUTE] &&
                    first[GregorianCalendar.HOUR_OF_DAY] == second[GregorianCalendar.HOUR_OF_DAY]
        }
    }
}
