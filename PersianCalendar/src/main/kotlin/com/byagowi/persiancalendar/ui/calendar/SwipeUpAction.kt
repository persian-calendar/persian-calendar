package com.byagowi.persiancalendar.ui.calendar

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class SwipeUpAction(@StringRes val titleId: Int) {
    Schedule(R.string.schedule),
    DayView(R.string.day_view),
    WeekView(R.string.week_view),
}
