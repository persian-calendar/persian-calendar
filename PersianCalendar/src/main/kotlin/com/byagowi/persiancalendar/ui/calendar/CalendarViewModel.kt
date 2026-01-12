package com.byagowi.persiancalendar.ui.calendar

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.shiftWorkSettings
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkViewModel
import com.byagowi.persiancalendar.ui.calendar.yearview.YearViewCommand
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.preferences
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

    private val _selectedTab = mutableStateOf(CalendarScreenTab.CALENDAR)
    val selectedTab by _selectedTab

    private val _isSearchOpen = mutableStateOf(false)
    val isSearchOpen by _isSearchOpen

    private val _searchTerm = mutableStateOf("")
    val searchTerm by _searchTerm

    private val _refreshToken = mutableIntStateOf(0)
    val refreshToken by _refreshToken

    private val _isHighlighted = mutableStateOf(false)
    val isHighlighted by _isHighlighted

    private val _removedThirdTab = mutableStateOf(false)
    val removedThirdTab by _removedThirdTab

    private val _shiftWorkViewModel = mutableStateOf<ShiftWorkViewModel?>(null)
    val shiftWorkViewModel by _shiftWorkViewModel

    private val _now = mutableLongStateOf(System.currentTimeMillis())
    val now by _now

    val todayButtonVisibility by derivedStateOf {
        isYearView || selectedMonthOffset != 0 || isHighlighted
    }

    private val _today = mutableStateOf(Jdn.today())
    val today by _today

    private val _isYearView = mutableStateOf(false)
    val isYearView by _isYearView

    private val _yearViewCommand = mutableStateOf<YearViewCommand?>(null)
    val yearViewCommand by _yearViewCommand

    private val _yearViewOffset = mutableIntStateOf(0)
    val yearViewOffset by _yearViewOffset

    private val _yearViewIsInYearSelection = mutableStateOf(false)
    val yearViewIsInYearSelection by _yearViewIsInYearSelection

    private val _daysScreenSelectedDay = mutableStateOf<Jdn?>(null)
    val daysScreenSelectedDay by _daysScreenSelectedDay

    private val _yearViewCalendar = mutableStateOf<Calendar?>(null)
    val yearViewCalendar by _yearViewCalendar

    // Commands
    fun changeYearViewCalendar(calendar: Calendar?) {
        _yearViewCalendar.value = calendar
    }

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

    fun changeSelectedTab(tab: CalendarScreenTab) {
        _selectedTab.value = tab
    }

    fun refreshCalendar() {
        ++_refreshToken.intValue
    }

    fun removeThirdTab() {
        _removedThirdTab.value = true
    }

    fun openSearch() {
        _isSearchOpen.value = true
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        changeSearchTerm("")
    }

    fun openShiftWorkDialog() {
        _shiftWorkViewModel.value = ShiftWorkViewModel.initiate(selectedDay, shiftWorkSettings)
    }

    fun closeShiftWorkDialog() {
        _shiftWorkViewModel.value = null
    }

    fun openYearView() {
        if (_yearViewCalendar.value == null) _yearViewCalendar.value = mainCalendar
        _isYearView.value = true
    }

    fun closeYearView() {
        _isYearView.value = false
    }

    fun changeSearchTerm(query: String) {
        _searchTerm.value = query
    }

    fun commandYearView(command: YearViewCommand) {
        _yearViewCommand.value = command
    }

    fun onYearViewBackPressed() {
        if (yearViewIsInYearSelection) commandYearView(YearViewCommand.ToggleYearSelection)
        else {
            changeYearViewCalendar(null)
            closeYearView()
        }
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

    fun initializeAndGetInitialSelectedTab(context: Context): CalendarScreenTab {
        val initialTab =
            CalendarScreenTab.entries.getOrNull(context.preferences.getInt(LAST_CHOSEN_TAB_KEY, 0))
                ?: CalendarScreenTab.entries[0]
        changeSelectedTab(initialTab)
        return initialTab
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
    }
}
