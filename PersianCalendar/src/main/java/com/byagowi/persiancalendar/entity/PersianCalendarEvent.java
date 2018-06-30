package com.byagowi.persiancalendar.entity;

import calendar.PersianDate;

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
public class PersianCalendarEvent {
    private PersianDate date;
    private String title;
    private boolean holiday;

    public PersianCalendarEvent(PersianDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }

    public PersianDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHoliday() {
        return holiday;
    }
}