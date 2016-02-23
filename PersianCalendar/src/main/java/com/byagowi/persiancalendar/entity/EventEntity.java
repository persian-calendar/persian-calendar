package com.byagowi.persiancalendar.entity;

import calendar.PersianDate;

/**
 * EventEntity
 *
 * @author ebraminio
 */
public class EventEntity {
    private PersianDate date;
    private String title;
    private boolean holiday;

    public EventEntity(PersianDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }

    public PersianDate getDate() {
        return date;
    }

    public void setDate(PersianDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public void setHoliday(boolean holiday) {
        this.holiday = holiday;
    }
}