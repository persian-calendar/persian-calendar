package com.byagowi.persiancalendar.entity;

public class DayEntity {
    private boolean today;
    private long jdn;
    private int dayOfWeek;

    public boolean isToday() {
        return today;
    }

    public void setToday(boolean today) {
        this.today = today;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public long getJdn() {
        return jdn;
    }

    public void setJdn(long jdn) {
        this.jdn = jdn;
    }
}
