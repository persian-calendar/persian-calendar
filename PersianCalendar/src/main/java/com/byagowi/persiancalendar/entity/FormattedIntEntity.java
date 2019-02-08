package com.byagowi.persiancalendar.entity;

import androidx.annotation.NonNull;

public class FormattedIntEntity {
    final private int value;
    final private String title;

    public FormattedIntEntity(int value, String title) {
        this.value = value;
        this.title = title;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
