package com.byagowi.persiancalendar.view.drugalert.model;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DrugUnit {

	private int quantity;

	private String unit;

	public DrugUnit(int quantity, String unit) {
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
