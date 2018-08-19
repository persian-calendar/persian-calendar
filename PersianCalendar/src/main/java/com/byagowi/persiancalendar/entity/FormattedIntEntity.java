package com.byagowi.persiancalendar.entity;

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

    @Override
    public String toString() {
        return title;
    }
}
