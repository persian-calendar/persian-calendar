package com.byagowi.persiancalendar.entities;

import com.byagowi.persiancalendar.calendar.CivilDate;

public class GregorianCalendarEvent extends AbstractEvent<CivilDate> {
    public GregorianCalendarEvent(CivilDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }
}
