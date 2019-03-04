package com.byagowi.persiancalendar.entities;

import com.byagowi.persiancalendar.calendar.IslamicDate;

public class IslamicCalendarEvent extends AbstractEvent<IslamicDate> {
    public IslamicCalendarEvent(IslamicDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }
}
