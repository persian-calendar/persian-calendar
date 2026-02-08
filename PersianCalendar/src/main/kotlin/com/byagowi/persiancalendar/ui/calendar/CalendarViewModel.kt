package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.resumeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class CalendarViewModel() : ViewModel() {
    private val _selectedDay = mutableStateOf(Jdn.today())
    val selectedDay by _selectedDay

    private val _selectedMonthOffset = mutableIntStateOf(0)
    val selectedMonthOffset by _selectedMonthOffset

    private val _selectedMonthOffsetCommand = mutableStateOf<Int?>(null)
    val selectedMonthOffsetCommand by _selectedMonthOffsetCommand

    private val _refreshToken = mutableIntStateOf(0)
    val refreshToken by _refreshToken

    private val _isHighlighted = mutableStateOf(false)
    val isHighlighted by _isHighlighted

    private val _daysScreenSelectedDay = mutableStateOf<Jdn?>(null)
    val daysScreenSelectedDay by _daysScreenSelectedDay

    fun changeDaysScreenSelectedDay(jdn: Jdn?) {
        jdn?.let { changeSelectedDay(it) }
        _daysScreenSelectedDay.value = jdn
    }

    fun changeSelectedMonthOffsetCommand(offset: Int?) {
        _selectedMonthOffsetCommand.value = offset
    }

    /**
     * This is just to notify the readers of selectedMonthOffset,
     * use [changeSelectedMonthOffsetCommand] for actually altering viewpager's state
     */
    fun notifySelectedMonthOffset(offset: Int) {
        _selectedMonthOffset.intValue = offset
    }

    fun changeSelectedDay(jdn: Jdn) {
        _isHighlighted.value = true
        _selectedDay.value = jdn
    }

    fun refreshCalendar() {
        ++_refreshToken.intValue
    }

    fun bringDay(jdn: Jdn, highlight: Boolean = true) {
        changeSelectedDay(jdn)
        if (!highlight) _isHighlighted.value = false
        changeSelectedMonthOffsetCommand(mainCalendar.getMonthsDistance(Jdn.today(), jdn))
    }

    init {
        viewModelScope.launch {
            var today = Jdn.today()
            while (true) {
                delay(30.seconds)
                val newToday = Jdn.today()
                if (today != newToday) {
                    refreshCalendar()
                    today = newToday
                }
            }
        }
        viewModelScope.launch {
            snapshotFlow { resumeToken }.collect {
                if (it > 1) {
                    delay(.5.seconds)
                    refreshCalendar()
                    delay(.5.seconds)
                    refreshCalendar()
                }
            }
        }
    }
}
