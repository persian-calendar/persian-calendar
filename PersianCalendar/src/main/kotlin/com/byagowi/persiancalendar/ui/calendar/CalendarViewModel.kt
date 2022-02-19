package com.byagowi.persiancalendar.ui.calendar

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class CalendarViewModel : ViewModel() {
    val selectedDay = MutableStateFlow(Jdn.today())
    val selectedMonth = MutableStateFlow(
        mainCalendar.getMonthStartFromMonthsDistance(selectedDay.value, 0)
    )
    val todayButtonVisibility = merge(selectedDay, selectedMonth).map {
        val todayJdn = Jdn.today()
        val todayDate = todayJdn.toCalendar(mainCalendar)
        val selectedMonth = selectedMonth.value
        val jdn = selectedDay.value
        selectedMonth.year != todayDate.year || selectedMonth.month != todayDate.month ||
                jdn != todayJdn
    }
    val selectedTab = MutableStateFlow(0)
}
