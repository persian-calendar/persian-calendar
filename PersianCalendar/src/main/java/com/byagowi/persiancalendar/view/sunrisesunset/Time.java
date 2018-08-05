package com.byagowi.persiancalendar.view.sunrisesunset;

public class Time {

    public static final int MINUTES_PER_HOUR = 60;

    public int hour;

    public int minute;

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int transformToMinutes() {
        return hour * MINUTES_PER_HOUR + minute;
    }
}
