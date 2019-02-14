package com.byagowi.persiancalendar.view.reminder.model;

import java.util.Date;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderDetails {
	
	private long id;
	private String reminderName;
	private String reminderInfo;
	private ReminderUnit reminderPeriod;
	private Date setStartTime;

	public ReminderDetails() {
		id = -1;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setReminderName(String reminderName) {
		this.reminderName = reminderName;
	}
	
	public String getReminderName() {
		return reminderName;
	}
	
	public void setReminderInfo(String reminderInfo) {
		this.reminderInfo = reminderInfo;
	}
	
	public String getReminderInfo() {
		return reminderInfo;
	}
	
	public void setReminderPeriod(ReminderUnit reminderPeriod) {
		this.reminderPeriod = reminderPeriod;
	}
	
	public ReminderUnit getReminderPeriod() {
		return reminderPeriod;
	}
	
	public void setStartTime(Date start_time) {
		this.setStartTime = start_time;
	}
	
	public Date getStartTime() {
		return setStartTime;
	}

}
