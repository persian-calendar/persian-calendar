package com.byagowi.persiancalendar.ui.calendar

import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.empty
import com.byagowi.persiancalendar.shared.generated.resources.month_view
import com.byagowi.persiancalendar.shared.generated.resources.schedule
import com.byagowi.persiancalendar.shared.generated.resources.week_view
import com.byagowi.persiancalendar.shared.generated.resources.year_view
import org.jetbrains.compose.resources.StringResource

enum class SwipeUpAction(val titleRes: StringResource) {
    WeekView(Res.string.week_view), Schedule(Res.string.schedule), None(Res.string.empty),
}

enum class SwipeDownAction(val titleRes: StringResource) {
    YearView(Res.string.year_view), MonthView(Res.string.month_view), None(Res.string.empty),
}
