package com.byagowi.persiancalendar.entity;

import calendar.CivilDate;

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
public class GregorianCalendarEvent extends AbstractEvent<CivilDate> {
    public GregorianCalendarEvent(CivilDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }
}