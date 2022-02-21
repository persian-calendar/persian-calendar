package com.byagowi.persiancalendar.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class CalendarViewModel : ViewModel() {
    val selectedDay = MutableStateFlow(Jdn.today())
    val selectedMonth = MutableStateFlow(
        mainCalendar.getMonthStartFromMonthsDistance(selectedDay.value, 0)
    )
    val todayButtonVisibility = selectedDay.combine(selectedMonth) { selectedDay, selectedMonth ->
        val todayJdn = Jdn.today()
        val todayDate = todayJdn.toCalendar(mainCalendar)
        selectedMonth.year != todayDate.year || selectedMonth.month != todayDate.month ||
                selectedDay != todayJdn
    }
    val selectedTab = MutableStateFlow(0)

    private val repository: ISearchEventsRepository = SearchEventsRepository() // TODO: Inject maybe
    suspend fun loadEvents(context: Context) = repository.createStore(context)
}
