package com.byagowi.persiancalendar.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class CalendarViewModel : ViewModel() {
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

    fun changeSelectedMonth(selectedMonth: AbstractDate) {
        _selectedMonth.value = selectedMonth
    }

    fun changeSelectedDay(jdn: Jdn) {
        _selectedDay.value = jdn
    }

    fun changeSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    private val repository: ISearchEventsRepository = SearchEventsRepository() // TODO: Inject maybe
    suspend fun loadEvents(context: Context) = repository.createStore(context)
}
