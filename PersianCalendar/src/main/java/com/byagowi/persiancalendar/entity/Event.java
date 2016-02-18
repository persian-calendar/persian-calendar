package com.byagowi.persiancalendar.entity;

import calendar.PersianDate;

/**
 * Event POJO
 *
 * @author ebraminio
 */
public class Event {
    private PersianDate date;
    private String title;

    public Event(PersianDate date, String title) {
        this.date = date;
        this.title = title;
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
}