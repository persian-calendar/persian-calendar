package com.byagowi.persiancalendar.entity;

import calendar.CalendarType;

public class CalendarTypeEntity {
    private final CalendarType type;
    private final String title;

    public CalendarTypeEntity(CalendarType type, String title) {
        this.type = type;
        this.title = title;
    }

    public CalendarType getType() {
        return type;
    }

    public String toString() {
        return title;
    }
}
