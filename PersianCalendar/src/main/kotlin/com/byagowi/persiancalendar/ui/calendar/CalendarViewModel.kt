package com.byagowi.persiancalendar.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsStore
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CalendarViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ISearchEventsRepository = SearchEventsRepository() // TODO: Inject maybe
) : AndroidViewModel(application) {
    private val _selectedDay = MutableStateFlow(Jdn.today())
    val selectedDay: StateFlow<Jdn> get() = _selectedDay

    private val _selectedMonth = MutableStateFlow(
        mainCalendar.getMonthStartFromMonthsDistance(selectedDay.value, 0)
    )
    val selectedMonth: StateFlow<AbstractDate> get() = _selectedMonth

    val todayButtonVisibility = selectedDay.combine(selectedMonth) { selectedDay, selectedMonth ->
        val todayJdn = Jdn.today()
        val todayDate = todayJdn.toCalendar(mainCalendar)
        selectedMonth.year != todayDate.year || selectedMonth.month != todayDate.month ||
                selectedDay != todayJdn
    }

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> get() = _selectedTabIndex

    private var _store: SearchEventsStore? = null
    val store: SearchEventsStore get() = requireNotNull(_store)

    init {
        viewModelScope.launch {
            _store = repository.createStore(application)
        }
    }

    fun changeSelectedMonth(selectedMonth: AbstractDate) {
        _selectedMonth.value = selectedMonth
    }

    fun changeSelectedDay(jdn: Jdn) {
        _selectedDay.value = jdn
    }

    fun changeSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }
}
