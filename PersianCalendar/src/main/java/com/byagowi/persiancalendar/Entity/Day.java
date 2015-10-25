package com.byagowi.persiancalendar.Entity;

/**
 * Created by behdad on 10/25/15.
 */
public class Day {
    String num;
    boolean holiday;
    boolean today;
    int dayOfWeek;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
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

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
