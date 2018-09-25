package com.byagowi.persiancalendar.entity;

import com.byagowi.persiancalendar.calendar.CivilDate;

import java.util.Date;

public class DeviceCalendarEvent extends AbstractEvent<CivilDate> {
    private int id;
    private String description;
    private String dateString;
    private Date start;
    private Date end;
    private String color;

    public DeviceCalendarEvent(int id, String title, String description,
                               Date start, Date end, String dateString, CivilDate date,
                               String color) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.dateString = dateString;
        this.date = date;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    @Override
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

    public String getDateString() {
        return dateString;
    }

    public String getColor() {
        return color;
    }
}
