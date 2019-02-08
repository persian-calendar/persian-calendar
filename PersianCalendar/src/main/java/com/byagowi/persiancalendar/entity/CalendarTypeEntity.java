package com.byagowi.persiancalendar.entity;

import com.byagowi.persiancalendar.util.CalendarType;

import androidx.annotation.NonNull;

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

    @NonNull
    public String toString() {
        return title;
    }
}
