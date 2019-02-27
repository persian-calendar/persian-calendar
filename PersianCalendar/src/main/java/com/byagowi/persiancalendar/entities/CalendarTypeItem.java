package com.byagowi.persiancalendar.entities;

import com.byagowi.persiancalendar.utils.CalendarType;

import androidx.annotation.NonNull;

public class CalendarTypeItem {
    private final CalendarType type;
    private final String title;

    public CalendarTypeItem(CalendarType type, String title) {
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
