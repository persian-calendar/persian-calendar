package com.byagowi.persiancalendar.shared;

import org.json.JSONException;
import org.json.JSONObject;

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
        try {
            JSONObject result = new JSONObject();
            result.put("title", title);
            result.put("text", text);
            result.put("icon", icon);
            result.put("season", season.name());
            return result.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public static NotificationItem newInstance(String object) {
        try {
            JSONObject json = new JSONObject(object);
            return new NotificationItem(json.getString("title"), json.getString("text"),
                    json.getInt("icon"), SeasonEnum.valueOf(json.getString("season")));
        } catch (JSONException e) {
            return null;
        }
    }
}
