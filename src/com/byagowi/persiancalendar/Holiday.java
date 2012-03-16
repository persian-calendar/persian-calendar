/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
package com.byagowi.persiancalendar;

import calendar.PersianDate;

/**
 * Holiday POJO
 * @author ebraminio
 *
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