package com.alirezaafkar.persiancalendar.shared;

import com.google.gson.Gson;

/**
 * Created by Alireza Afkar on 3/15/16 AD.
 */
public class NotificationItem {
    private SeasonEnum season;
    private String title;
    private String text;
    private int icon;

    public NotificationItem(String title, String text, int icon, SeasonEnum season) {
        this.season = season;
        this.title = title;
        this.text = text;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public SeasonEnum getSeason() {
        return season;
    }

    public void setSeason(SeasonEnum season) {
        this.season = season;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static NotificationItem newInstance(String object) {
        return new Gson().fromJson(object, NotificationItem.class);
    }
}
