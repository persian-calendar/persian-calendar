package com.byagowi.persiancalendar.ui.shared

import com.byagowi.persiancalendar.utils.CalendarType

interface DayPickerView {

    val dayJdnFromView: Long

    val selectedCalendarType: CalendarType
    fun setDayJdnOnView(jdn: Long)

    fun setOnSelectedDayChangedListener(listener: (Long) -> Unit)

    interface OnSelectedDayChangedListener {
        fun onSelectedDayChanged(jdn: Long)
    }
}
