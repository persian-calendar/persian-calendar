package com.byagowi.persiancalendar.ui.calendar

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkViewModel
import com.byagowi.persiancalendar.ui.resumeToken
import com.byagowi.persiancalendar.utils.HALF_SECOND_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CalendarViewModel @JvmOverloads constructor(
    application: Application,
    private var repository: ISearchEventsRepository = ISearchEventsRepository.empty() // TODO: Inject maybe
) : AndroidViewModel(application) {
    private val _selectedDay = MutableStateFlow(Jdn.today())
    val selectedDay: StateFlow<Jdn> get() = _selectedDay

    private val _selectedMonthOffset = MutableStateFlow(0)
    val selectedMonthOffset: StateFlow<Int> get() = _selectedMonthOffset

    private val _selectedMonthOffsetCommand = MutableStateFlow<Int?>(null)
    val selectedMonthOffsetCommand: StateFlow<Int?> get() = _selectedMonthOffsetCommand

    private val _selectedTabIndex = MutableStateFlow(CALENDARS_TAB)
    val selectedTabIndex: StateFlow<Int> get() = _selectedTabIndex

    private val _isSearchOpenFlow = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpenFlow

    private val _eventsFlow = MutableSharedFlow<List<CalendarEvent<*>>>()
    val eventsFlow: SharedFlow<List<CalendarEvent<*>>> get() = _eventsFlow

    private val _refreshToken = MutableStateFlow(0)
    val refreshToken: StateFlow<Int> = _refreshToken

    private val _isHighlighted = MutableStateFlow(false)
    val isHighlighted: StateFlow<Boolean> = _isHighlighted

    private val _removedThirdTab = MutableStateFlow(false)
    val removedThirdTab: StateFlow<Boolean> = _removedThirdTab

    private val _shiftWorkViewModel = MutableStateFlow<ShiftWorkViewModel?>(null)
    val shiftWorkViewModel: StateFlow<ShiftWorkViewModel?> = _shiftWorkViewModel

    private val _sunViewNeedAnimation = MutableStateFlow(false)
    val sunViewNeedsAnimation: StateFlow<Boolean> = _sunViewNeedAnimation

    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> = _now

    private val _today = MutableStateFlow(Jdn.today())
    val today: StateFlow<Jdn> = _today

    // Commands
    fun changeSelectedMonthOffset(offset: Int) {
        _selectedMonthOffset.value = offset
    }

    fun changeSelectedMonthOffsetCommand(offset: Int?) {
        _selectedMonthOffsetCommand.value = offset
    }

    fun changeSelectedDay(jdn: Jdn) {
        _isHighlighted.value = true
        _selectedDay.value = jdn
    }

    fun clearHighlightedDay() {
        _isHighlighted.value = false
    }

    fun changeSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    fun refreshCalendar() {
        ++_refreshToken.value
    }

    fun removeThirdTab() {
        _removedThirdTab.value = true
    }

    fun openSearch() {
        _isSearchOpenFlow.value = true
    }

    fun closeSearch() {
        _isSearchOpenFlow.value = false
    }

    fun searchEvent(query: CharSequence) {
        viewModelScope.launch { _eventsFlow.emit(repository.findEvent(query)) }
    }

    fun setShiftWorkViewModel(shiftWorkViewModel: ShiftWorkViewModel?) {
        _shiftWorkViewModel.value = shiftWorkViewModel
    }

    fun clearNeedsAnimation() {
        _sunViewNeedAnimation.value = false
    }

    fun astronomicalOverviewLaunched() {
        _sunViewNeedAnimation.value = true
    }

    // Events store cache needs to be invalidated as preferences of enabled events can be changed
    // or user has added an appointment on their calendar outside the app.
    fun initializeEventsRepository() {
        repository = SearchEventsRepository(getApplication())
    }


    init {
        viewModelScope.launch {
            val prefs = application.appPrefs
            changeSelectedTabIndex(prefs.getInt(LAST_CHOSEN_TAB_KEY, 0))
            selectedTabIndex.collectLatest { prefs.edit { putInt(LAST_CHOSEN_TAB_KEY, it) } }
        }
        viewModelScope.launch {
            selectedTabIndex.combine(selectedDay) { tabIndex, day ->
                tabIndex == TIMES_TAB && day == today.value
            }.collect { if (it) _sunViewNeedAnimation.value = true }
        }
        viewModelScope.launch {
            while (true) {
                delay(THIRTY_SECONDS_IN_MILLIS)
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
                delay(HALF_SECOND_IN_MILLIS)
                refreshCalendar()
                delay(HALF_SECOND_IN_MILLIS)
                refreshCalendar()
            }
        }
    }
}
