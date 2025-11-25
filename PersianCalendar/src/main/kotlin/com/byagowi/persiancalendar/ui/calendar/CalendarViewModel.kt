package com.byagowi.persiancalendar.ui.calendar

import android.app.Application
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkViewModel
import com.byagowi.persiancalendar.ui.calendar.yearview.YearViewCommand
import com.byagowi.persiancalendar.ui.resumeToken
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.createSearchRegex
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.searchDeviceCalendarEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val _selectedDay = MutableStateFlow(Jdn.today())
    val selectedDay: StateFlow<Jdn> get() = _selectedDay

    private val _selectedMonthOffset = MutableStateFlow(0)
    val selectedMonthOffset: StateFlow<Int> get() = _selectedMonthOffset

    private val _selectedMonthOffsetCommand = MutableStateFlow<Int?>(null)
    val selectedMonthOffsetCommand: StateFlow<Int?> get() = _selectedMonthOffsetCommand

    private val _selectedTab = MutableStateFlow(CalendarScreenTab.CALENDAR)
    val selectedTab: StateFlow<CalendarScreenTab> get() = _selectedTab

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> get() = _isSearchOpen

    private val _foundItems = MutableStateFlow<List<CalendarEvent<*>>>(emptyList())
    val foundItems: StateFlow<List<CalendarEvent<*>>> get() = _foundItems

    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> get() = _searchTerm

    private val _refreshToken = MutableStateFlow(0)
    val refreshToken: StateFlow<Int> get() = _refreshToken

    private val _isHighlighted = MutableStateFlow(false)
    val isHighlighted: StateFlow<Boolean> get() = _isHighlighted

    private val _removedThirdTab = MutableStateFlow(false)
    val removedThirdTab: StateFlow<Boolean> get() = _removedThirdTab

    private val _shiftWorkViewModel = MutableStateFlow<ShiftWorkViewModel?>(null)
    val shiftWorkViewModel: StateFlow<ShiftWorkViewModel?> get() = _shiftWorkViewModel

    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> get() = _now

    private val _todayButtonVisibility = MutableStateFlow(false)
    val todayButtonVisibility: StateFlow<Boolean> get() = _todayButtonVisibility

    private val _today = MutableStateFlow(Jdn.today())
    val today: StateFlow<Jdn> get() = _today

    private val _isYearView = MutableStateFlow(false)
    val isYearView: StateFlow<Boolean> get() = _isYearView

    private val _yearViewCommand = MutableStateFlow<YearViewCommand?>(null)
    val yearViewCommand: StateFlow<YearViewCommand?> get() = _yearViewCommand

    private val _yearViewOffset = MutableStateFlow(0)
    val yearViewOffset: StateFlow<Int> get() = _yearViewOffset

    private val _yearViewIsInYearSelection = MutableStateFlow(false)
    val yearViewIsInYearSelection: StateFlow<Boolean> get() = _yearViewIsInYearSelection

    private val _daysScreenSelectedDay = MutableStateFlow<Jdn?>(null)
    val daysScreenSelectedDay: StateFlow<Jdn?> get() = _daysScreenSelectedDay

    private val _yearViewCalendar = MutableStateFlow<Calendar?>(null)
    val yearViewCalendar: StateFlow<Calendar?> get() = _yearViewCalendar

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
        _yearViewOffset.value = value
    }

    /**
     * This is just to notify the readers of selectedMonthOffset,
     * use [changeSelectedMonthOffsetCommand] for actually altering viewpager's state
     */
    fun notifySelectedMonthOffset(offset: Int) {
        _selectedMonthOffset.value = offset
    }

    fun changeSelectedDay(jdn: Jdn) {
        _isHighlighted.value = true
        _selectedDay.value = jdn
    }

    fun changeSelectedTab(tab: CalendarScreenTab) {
        _selectedTab.value = tab
    }

    fun refreshCalendar() {
        ++_refreshToken.value
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
        enabledEvents.value = emptyList()
        _foundItems.value = emptyList()
    }

    fun setShiftWorkViewModel(shiftWorkViewModel: ShiftWorkViewModel?) {
        _shiftWorkViewModel.value = shiftWorkViewModel
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

    private val enabledEvents = MutableStateFlow(emptyList<CalendarEvent<*>>())

    fun initializeEventsStore(repository: EventsRepository) {
        enabledEvents.value = repository.getEnabledEvents(Jdn.today())
    }

    fun commandYearView(command: YearViewCommand) {
        _yearViewCommand.value = command
    }

    fun onYearViewBackPressed() {
        if (yearViewIsInYearSelection.value) commandYearView(YearViewCommand.ToggleYearSelection)
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
                    baseJdn = today.value,
                    monthsDistance = selectedMonthOffset.value,
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
        changeSelectedMonthOffsetCommand(mainCalendar.getMonthsDistance(today.value, jdn))

        // a11y
        if (isTalkBackEnabled.value && jdn != today.value) {
            val context = getApplication<Application>()
            Toast.makeText(
                context,
                getA11yDaySummary(
                    resources = context.resources,
                    jdn = jdn,
                    isToday = false,
                    deviceCalendarEvents = EventsStore.empty(),
                    withZodiac = true,
                    withOtherCalendars = true,
                    withTitle = true
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    init {
        viewModelScope.launch {
            val preferences = application.preferences
            changeSelectedTab(
                CalendarScreenTab.entries.getOrNull(preferences.getInt(LAST_CHOSEN_TAB_KEY, 0))
                    ?: CalendarScreenTab.entries[0]
            )
            selectedTab.collectLatest {
                preferences.edit { putInt(LAST_CHOSEN_TAB_KEY, it.ordinal) }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(30.seconds)
                _now.value = System.currentTimeMillis()
                val today = Jdn.today()
                if (_today.value != today) {
                    refreshCalendar()
                    _today.value = today
                    if (!isHighlighted.value) _selectedDay.value = today
                }
            }
        }
        viewModelScope.launch {
            resumeToken.collect {
                delay(.5.seconds)
                refreshCalendar()
                delay(.5.seconds)
                refreshCalendar()
            }
        }
        viewModelScope.launch {
            merge(selectedMonthOffset, isHighlighted, isYearView).collectLatest {
                _todayButtonVisibility.value =
                    isYearView.value || selectedMonthOffset.value != 0 || isHighlighted.value
            }
        }
        viewModelScope.launch {
            merge(searchTerm, enabledEvents).collectLatest {
                val deviceEvents =
                    getApplication<Application>().searchDeviceCalendarEvents(searchTerm.value)
                val events = if (searchTerm.value.isBlank()) emptyList() else {
                    val regex = createSearchRegex(searchTerm.value)
                    enabledEvents.value.asSequence().filter { regex.containsMatchIn(it.title) }
                        .take(50).toList()
                }
                _foundItems.value = deviceEvents + events
            }
        }
    }
}
