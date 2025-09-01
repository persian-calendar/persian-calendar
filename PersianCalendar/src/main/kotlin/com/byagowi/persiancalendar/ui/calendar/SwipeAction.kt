package com.byagowi.persiancalendar.ui.calendar

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class SwipeUpAction(@get:StringRes val titleId: Int) {
    WeekView(R.string.week_view),
    DayView(R.string.day_view),
    Schedule(R.string.schedule),
    None(R.string.empty),
}

enum class SwipeDownAction(@get:StringRes val titleId: Int) {
    YearView(R.string.year_view),

    //    MonthView(R.string.month_view),
    None(R.string.empty),
}
