package com.byagowi.persiancalendar.entity;

import calendar.AbstractDate;

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
abstract public class AbstractEvent<T extends AbstractDate> {
    String title;
    boolean holiday;
    T date;

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