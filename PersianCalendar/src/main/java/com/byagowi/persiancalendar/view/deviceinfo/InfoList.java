package com.byagowi.persiancalendar.view.deviceinfo;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class InfoList {
    private String title, content, ver;

    public InfoList() {
    }

    public InfoList(String title, String content, String ver) {
        this.title = title;
        this.content = content;
        this.ver = ver;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
