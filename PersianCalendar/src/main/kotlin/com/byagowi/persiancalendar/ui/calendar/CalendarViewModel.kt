package com.byagowi.persiancalendar.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CalendarViewModel @JvmOverloads constructor(
    application: Application,
    private var repository: ISearchEventsRepository = ISearchEventsRepository.empty() // TODO: Inject maybe
) : AndroidViewModel(application) {

    // State
    private val _selectedDay = MutableStateFlow(Jdn.today())
    private val _selectedMonth = MutableStateFlow(
        mainCalendar.getMonthStartFromMonthsDistance(selectedDay.value, 0)
    )
    private val _selectedTabIndex = MutableStateFlow(0)
    private val _eventsFlow = MutableStateFlow<List<CalendarEvent<*>>>(emptyList())

    // Subscriptions
    val selectedDay: StateFlow<Jdn> get() = _selectedDay
    val selectedMonth: StateFlow<AbstractDate> get() = _selectedMonth
    val selectedTabIndex: StateFlow<Int> get() = _selectedTabIndex
    val eventsFlow: StateFlow<List<CalendarEvent<*>>> get() = _eventsFlow
    val todayButtonVisibility = selectedDay.combine(selectedMonth) { selectedDay, selectedMonth ->
        val todayJdn = Jdn.today()
        val todayDate = todayJdn.toCalendar(mainCalendar)
        selectedMonth.year != todayDate.year || selectedMonth.month != todayDate.month ||
                selectedDay != todayJdn
    }

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

    fun searchEvent(query: CharSequence) {
        viewModelScope.launch { _eventsFlow.value = repository.findEvent(query) }
    }

    // Events store cache needs to be invalidated as preferences of enabled events can be changed
    // or user has added an appointment on their calendar outside the app.
    fun initializeEventsRepository() {
        repository = SearchEventsRepository(getApplication())
    }
}
