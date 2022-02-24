package com.byagowi.persiancalendar.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import com.byagowi.persiancalendar.utils.EnabledHolidays
import com.byagowi.persiancalendar.utils.appPrefs
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CalendarViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ISearchEventsRepository = SearchEventsRepository(application) // TODO: Inject maybe
) : AndroidViewModel(application) {

    // State
    private val _selectedDay = MutableStateFlow(Jdn.today())
    private val _selectedMonth = MutableStateFlow(
        mainCalendar.getMonthStartFromMonthsDistance(selectedDay, 0)
    )
    private val _selectedTabIndex = MutableStateFlow(0)
    private val _eventsFlow = MutableStateFlow<List<CalendarEvent<*>>>(emptyList())

    // Values
    val selectedDay: Jdn get() = _selectedDay.value
    val selectedMonth: StateFlow<AbstractDate> get() = _selectedMonth
    val selectedTabIndex: StateFlow<Int> get() = _selectedTabIndex
    val eventsFlow: StateFlow<List<CalendarEvent<*>>> get() = _eventsFlow

    // Events
    val selectedDayChangeEvent: Flow<Jdn> get() = _selectedDay
    val todayButtonVisibilityEvent = _selectedDay.combine(_selectedMonth) { day, month ->
        val todayJdn = Jdn.today()
        val todayDate = todayJdn.toCalendar(mainCalendar)
        month.year != todayDate.year || month.month != todayDate.month || day != todayJdn
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
        viewModelScope.launch {
            _eventsFlow.value =
                repository.findEvent(query, EnabledHolidays(getApplication<Application>().appPrefs))
        }
    }
}
