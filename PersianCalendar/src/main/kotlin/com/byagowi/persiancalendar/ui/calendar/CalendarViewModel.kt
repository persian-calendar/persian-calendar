package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.yearview.YearViewCommand
import com.byagowi.persiancalendar.ui.resumeToken
import com.byagowi.persiancalendar.utils.calendar
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

    private val _now = mutableLongStateOf(System.currentTimeMillis())
    val now by _now

    private val _today = mutableStateOf(Jdn.today())
    val today by _today

    private val _yearViewCommand = mutableStateOf<YearViewCommand?>(null)
    val yearViewCommand by _yearViewCommand

    private val _yearViewOffset = mutableIntStateOf(0)
    val yearViewOffset by _yearViewOffset

    private val _yearViewIsInYearSelection = mutableStateOf(false)
    val yearViewIsInYearSelection by _yearViewIsInYearSelection

    private val _daysScreenSelectedDay = mutableStateOf<Jdn?>(null)
    val daysScreenSelectedDay by _daysScreenSelectedDay

    fun changeDaysScreenSelectedDay(jdn: Jdn?) {
        jdn?.let { changeSelectedDay(it) }
        _daysScreenSelectedDay.value = jdn
    }

    fun changeSelectedMonthOffsetCommand(offset: Int?) {
        _selectedMonthOffsetCommand.value = offset
    }

    fun notifyYearViewOffset(value: Int) {
        _yearViewOffset.intValue = value
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

    fun commandYearView(command: YearViewCommand) {
        _yearViewCommand.value = command
    }

    fun clearYearViewCommand() {
        _yearViewCommand.value = null
    }

    fun yearViewIsInYearSelection(value: Boolean) {
        _yearViewIsInYearSelection.value = value
    }

    fun bringEvent(event: CalendarEvent<*>) {
        val date = event.date
        val calendar = date.calendar
        val jdn = Jdn(
            calendar = calendar,
            year = date.year.takeIf { it != -1 } ?: run {
                val selectedMonth = calendar.getMonthStartFromMonthsDistance(
                    baseJdn = today,
                    monthsDistance = selectedMonthOffset,
                )
                selectedMonth.year + if (date.month < selectedMonth.month) 1 else 0
            },
            month = date.month,
            day = date.dayOfMonth,
        )
        bringDay(jdn)
    }

    fun bringDay(jdn: Jdn, highlight: Boolean = true) {
        changeSelectedDay(jdn)
        if (!highlight) _isHighlighted.value = false
        changeSelectedMonthOffsetCommand(mainCalendar.getMonthsDistance(today, jdn))
    }

    init {
        viewModelScope.launch {
            while (true) {
                delay(30.seconds)
                _now.longValue = System.currentTimeMillis()
                val today = Jdn.today()
                if (_today.value != today) {
                    refreshCalendar()
                    _today.value = today
                    if (!isHighlighted) _selectedDay.value = today
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
