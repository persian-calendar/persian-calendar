package com.byagowi.persiancalendar.entity;

import calendar.IslamicDate;

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
public class IslamicCalendarEvent extends AbstractEvent<IslamicDate> {
    public IslamicCalendarEvent(IslamicDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }
}