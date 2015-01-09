package com.byagowi.persiancalendar;

import calendar.PersianDate;

/**
 * Holiday POJO
 *
 * @author ebraminio
 */
class Holiday {
    private PersianDate date;
    private String title;

    public Holiday(PersianDate date, String title) {
        this.date = date;
        this.title = title;
    }

    public PersianDate getDate() {
        return date;
    }

    public void setDate(PersianDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}