package com.byagowi.persiancalendar.entity;

public class DayEntity {
    final private boolean today;
    final private long jdn;
    final private int dayOfWeek;

    public DayEntity(boolean today, long jdn, int dayOfWeek) {
        this.today = today;
        this.jdn = jdn;
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isToday() {
        return today;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public long getJdn() {
        return jdn;
    }
}
