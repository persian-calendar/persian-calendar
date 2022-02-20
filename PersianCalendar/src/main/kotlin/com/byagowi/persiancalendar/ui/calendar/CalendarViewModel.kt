package com.byagowi.persiancalendar.ui.calendar

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.getAllEnabledAppointments
import com.byagowi.persiancalendar.utils.gregorianCalendarEvents
import com.byagowi.persiancalendar.utils.irregularCalendarEventsStore
import com.byagowi.persiancalendar.utils.islamicCalendarEvents
import com.byagowi.persiancalendar.utils.nepaliCalendarEvents
import com.byagowi.persiancalendar.utils.persianCalendarEvents
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
        context: Context, lifecycleOwner: LifecycleOwner,
        callback: (events: List<CalendarEvent<*>>, itemsWords: List<Pair<CalendarEvent<*>, List<String>>>) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) {
                // TODO: Inject today and global variable with some repository pattern instead
                val jdn = Jdn.today()
                val events = listOf(
                    context.getAllEnabledAppointments(), persianCalendarEvents.getAllEvents(),
                    islamicCalendarEvents.getAllEvents(), nepaliCalendarEvents.getAllEvents(),
                    gregorianCalendarEvents.getAllEvents(),
                ).flatten() + listOf(
                    jdn.toPersianCalendar(), jdn.toGregorianCalendar(), jdn.toIslamicCalendar()
                ).flatMap {
                    irregularCalendarEventsStore.getEventsList(it.year, it.calendarType)
                }
                val delimiters = arrayOf(" ", "(", ")", "-", /*ZWNJ*/"\u200c")
                val itemsWords = events.map { it to it.formattedTitle.split(*delimiters) }
                withContext(Dispatchers.Main.immediate) { callback(events, itemsWords) }
            }
        }
    }

    companion object {
        val CalendarEvent<*>.formattedTitle
            get() = when (this) {
                is CalendarEvent.GregorianCalendarEvent,
                is CalendarEvent.IslamicCalendarEvent,
                is CalendarEvent.PersianCalendarEvent,
                is CalendarEvent.NepaliCalendarEvent -> title
                is CalendarEvent.DeviceCalendarEvent ->
                    if (description.isBlank()) title else "$title ($description)"
            }
    }
}
