package com.byagowi.persiancalendar.entities;

import androidx.annotation.NonNull;

public class StringWithValueItem {
    final private int value;
    final private String title;

    public StringWithValueItem(int value, @NonNull String title) {
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
