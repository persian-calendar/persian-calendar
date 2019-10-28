package com.byagowi.persiancalendar.entities;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import io.github.persiancalendar.calendar.CivilDate;

/* CAUTION: Don't convert to Kotlin without testing device calendar events, apparently
   it doesn't work for some reason for now */
public class DeviceCalendarEvent implements BaseEvent {
    final private int id;
    final private String title;
    final private String description;
    final private String dateString;
    final private Date start;
    final private Date end;
    final private String color;
    final private CivilDate date;
    final private boolean isHoliday;

    public DeviceCalendarEvent(int id, String title, String description,
                               Date start, Date end, String dateString, CivilDate date,
                               String color, boolean isHoliday) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.dateString = dateString;
        this.date = date;
        this.color = color;
        this.isHoliday = isHoliday;
    }

    public int getId() {
        return id;
    }

    @NotNull
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

    public CivilDate getDate () {
        return date;
    }

    @Override
    public boolean isHoliday() {
        return isHoliday;
    }
}