package com.byagowi.persiancalendar.di.dependencies

import com.byagowi.persiancalendar.di.scopes.PerFragment
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
import com.byagowi.persiancalendar.ui.calendar.month.DaysPaintResources

import javax.inject.Inject

@PerFragment
class CalendarFragmentDependency @Inject
constructor(activity: MainActivity) {
    val daysPaintResources: DaysPaintResources = DaysPaintResources(activity)

    @Inject
    lateinit var calendarFragment: CalendarFragment internal set

}
