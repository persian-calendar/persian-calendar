package com.byagowi.persiancalendar.entity;

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
abstract public class AbstractEvent {
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
}