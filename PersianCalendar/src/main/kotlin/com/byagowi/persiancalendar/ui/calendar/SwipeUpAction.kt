package com.byagowi.persiancalendar.ui.calendar

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class SwipeUpAction(@StringRes val titleId: Int) {
    WeekView(R.string.week_view),
    DayView(R.string.day_view),
    Schedule(R.string.schedule),
}
