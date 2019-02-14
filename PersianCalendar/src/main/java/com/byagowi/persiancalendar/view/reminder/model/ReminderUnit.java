package com.byagowi.persiancalendar.view.reminder.model;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderUnit {

	private int quantity;

	private String unit;

	public ReminderUnit(int quantity, String unit) {
		this.quantity = quantity;
		this.unit = unit;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getUnit() {
		return unit;
	}

}
