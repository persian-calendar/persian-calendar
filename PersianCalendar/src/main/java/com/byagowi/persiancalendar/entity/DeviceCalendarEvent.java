package com.byagowi.persiancalendar.entity;

import java.util.Date;

import calendar.CivilDate;

public class DeviceCalendarEvent extends AbstractEvent {
    private int id;
    private String description;
    private Date start;
    private Date end;
    private String date;
    private CivilDate civilDate;

    public DeviceCalendarEvent(int id, String title, String description,
                               Date start, Date end, String date, CivilDate civilDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.date = date;
        this.civilDate = civilDate;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public String getDate() {
        return date;
    }

    public CivilDate getCivilDate() {
        return civilDate;
    }
}
