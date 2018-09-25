package com.byagowi.persiancalendar.entity;

import com.byagowi.persiancalendar.calendar.AbstractDate;

abstract public class AbstractEvent<T extends AbstractDate> {
    T date;
    String title;
    boolean holiday;

    public String getTitle() {
        return title;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public String toString() {
        return title;
    }

    public T getDate() {
        return date;
    }
}