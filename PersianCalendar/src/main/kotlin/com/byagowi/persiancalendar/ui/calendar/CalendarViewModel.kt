package com.byagowi.persiancalendar.ui.calendar

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.searchevent.ISearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

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

    fun loadEvents(
        context: Context, viewLifecycleScope: LifecycleCoroutineScope,
        callback: (eventsRepository: ISearchEventsRepository) -> Unit
    ) {
        viewLifecycleScope.launch(Dispatchers.IO) {
            withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) { // 2s timeout, give up if is too costly
                val eventsRepository = SearchEventsRepository(context) // TODO: inject?
                withContext(Dispatchers.Main.immediate) { callback(eventsRepository) }
            }
        }
    }
}
