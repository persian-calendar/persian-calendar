package com.byagowi.persiancalendar.ui.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarFragmentModel : ViewModel() {
    val monthFragmentsHandler = MutableLiveData<MonthFragmentUpdateCommand>()
    val selectedDayLiveData = MutableLiveData<Long>()
    var isTheFirstTime = true

    fun monthFragmentsUpdate(command: MonthFragmentUpdateCommand) {
        monthFragmentsHandler.postValue(command)
    }

    fun selectDay(jdn: Long) {
        selectedDayLiveData.postValue(jdn)
    }

    class MonthFragmentUpdateCommand internal constructor(val target: Int, val isEventsModification: Boolean, val currentlySelectedJdn: Long)
}
