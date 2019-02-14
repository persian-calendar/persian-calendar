package com.byagowi.persiancalendar.view.reminder.model;

import java.util.concurrent.TimeUnit;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderDetails {
	
	final public long id;
	final public String name;
	final public String info;
	final public TimeUnit unit;
	final public int quantity;
	final public long startTime;

	public ReminderDetails(long id, String name, String info, TimeUnit unit, int quantity,
						   long startTime) {
		this.id = id;
		this.name = name;
		this.info = info;
		this.unit = unit;
		this.quantity = quantity;
		this.startTime = startTime;
	}
}
