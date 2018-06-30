package com.byagowi.persiancalendar.entity;

import calendar.CivilDate;

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
public class GregorianCalendarEvent {
    private CivilDate date;
    private String title;
    private boolean holiday;

    public GregorianCalendarEvent(CivilDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }

    public CivilDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHoliday() {
        return holiday;
    }
}