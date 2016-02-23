package com.byagowi.persiancalendar.entity;

import calendar.PersianDate;

public class DayEntity {
    private String num;
    private boolean holiday;
    private boolean today;
    private int dayOfWeek;
    private PersianDate persianDate;
    private boolean event;

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

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

    public PersianDate getPersianDate() {
        return persianDate;
    }

    public void setPersianDate(PersianDate persianDate) {
        this.persianDate = persianDate;
    }
}
