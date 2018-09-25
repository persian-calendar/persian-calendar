package com.byagowi.persiancalendar.entity;

import calendar.PersianDate;

public class PersianCalendarEvent extends AbstractEvent<PersianDate> {
    public PersianCalendarEvent(PersianDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }
}