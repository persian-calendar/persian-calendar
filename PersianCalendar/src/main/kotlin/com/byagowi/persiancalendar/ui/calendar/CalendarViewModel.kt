package com.byagowi.persiancalendar.ui.calendar

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import com.byagowi.persiancalendar.utils.appPrefs
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalendarViewModel @JvmOverloads constructor(
    application: Application,
    private var repository: ISearchEventsRepository = ISearchEventsRepository.empty() // TODO: Inject maybe
) : AndroidViewModel(application) {
    private val _selectedDay = MutableStateFlow(Jdn.today())
    val selectedDay: StateFlow<Jdn> get() = _selectedDay

    private val _selectedMonth = MutableStateFlow(
        mainCalendar.getMonthStartFromMonthsDistance(selectedDay.value, 0)
    )
    val selectedMonth: StateFlow<AbstractDate> get() = _selectedMonth

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> get() = _selectedTabIndex

    private val _isSearchOpenFlow = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpenFlow

    private val _eventsFlow = MutableSharedFlow<List<CalendarEvent<*>>>()
    val eventsFlow: SharedFlow<List<CalendarEvent<*>>> get() = _eventsFlow

    // Commands
    fun changeSelectedMonth(selectedMonth: AbstractDate) {
        _selectedMonth.value = selectedMonth
    }

    fun changeSelectedDay(jdn: Jdn) {
        _selectedDay.value = jdn
    }

    fun changeSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
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
    }
}
