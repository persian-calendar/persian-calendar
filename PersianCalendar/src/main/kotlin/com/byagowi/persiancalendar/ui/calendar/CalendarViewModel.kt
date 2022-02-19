package com.byagowi.persiancalendar.ui.calendar

import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.entities.Jdn
import kotlinx.coroutines.flow.MutableStateFlow

class CalendarViewModel : ViewModel() {
    val jdn = MutableStateFlow(Jdn.today())
}
