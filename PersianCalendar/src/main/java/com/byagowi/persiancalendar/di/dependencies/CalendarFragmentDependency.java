package com.byagowi.persiancalendar.di.dependencies;

import com.byagowi.persiancalendar.di.scopes.PerFragment;
import com.byagowi.persiancalendar.ui.MainActivity;
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment;
import com.byagowi.persiancalendar.ui.calendar.month.DaysPaintResources;

import javax.inject.Inject;

@PerFragment
public final class CalendarFragmentDependency {
    private final DaysPaintResources daysPaintResources;

    @Inject
    CalendarFragment calendarFragment;

    @Inject
    public CalendarFragmentDependency(MainActivity activity) {
        daysPaintResources = new DaysPaintResources(activity);
    }

    public CalendarFragment getCalendarFragment() {
        return calendarFragment;
    }

    public DaysPaintResources getDaysPaintResources() {
        return daysPaintResources;
    }
}
