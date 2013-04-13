package com.byagowi.persiancalendar.utils;

import calendar.PersianDate;

/**
 * Holiday POJO
 * 
 * @author ebraminio
 */
public class Holiday {
	private PersianDate date;
	private String title;

	public PersianDate getDate() {
		return date;
	}

	public void setDate(PersianDate date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Holiday(PersianDate date, String title) {
		this.date = date;
		this.title = title;
	}
}