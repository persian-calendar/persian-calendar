package com.byagowi.persiancalendar.entity;

public class DayEntity {
    private boolean holiday;
    private boolean today;
    private long jdn;
    private boolean event;

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public void setHoliday(boolean holiday) {
        this.holiday = holiday;
    }

    public boolean isToday() {
        return today;
    }

    public void setToday(boolean today) {
        this.today = today;
    }

    public long getJdn() {
        return jdn;
    }

    public void setJdn(long jdn) {
        this.jdn = jdn;
    }
}
