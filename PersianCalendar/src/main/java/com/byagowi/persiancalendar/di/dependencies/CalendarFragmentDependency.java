package com.byagowi.persiancalendar.di.dependencies;

import com.byagowi.persiancalendar.di.scopes.PerFragment;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.byagowi.persiancalendar.view.itemdayview.DaysPaintResources;

import javax.inject.Inject;

@PerFragment
public final class CalendarFragmentDependency {
    private final DaysPaintResources daysPaintResources;

    @Inject
    public CalendarFragmentDependency(MainActivity activity) {
        daysPaintResources = new DaysPaintResources(activity);
    }

    @Inject
    CalendarFragment calendarFragment;

    public CalendarFragment getCalendarFragment() {
        return calendarFragment;
    }

    public DaysPaintResources getDaysPaintResources() {
        return daysPaintResources;
    }

}
