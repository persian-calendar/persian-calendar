package com.byagowi.persiancalendar.entity;

import calendar.IslamicDate;

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
public class IslamicCalendarEvent {
    private IslamicDate date;
    private String title;
    private boolean holiday;

    public IslamicCalendarEvent(IslamicDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }

    public IslamicDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHoliday() {
        return holiday;
    }
}