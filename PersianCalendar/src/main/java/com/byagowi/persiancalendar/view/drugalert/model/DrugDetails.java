package com.byagowi.persiancalendar.view.drugalert.model;

import java.util.Date;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DrugDetails {
	
	private long id;
	private String drugName;
	private String drugInfo;
	private DrugUnit drugPeriod;
	private Date setStartTime;

	public DrugDetails() {
		id = -1;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}
	
	public String getDrugName() {
		return drugName;
	}
	
	public void setDrugInfo(String drugInfo) {
		this.drugInfo = drugInfo;
	}
	
	public String getDrugInfo() {
		return drugInfo;
	}
	
	public void setDrugPeriod(DrugUnit drugPeriod) {
		this.drugPeriod = drugPeriod;
	}
	
	public DrugUnit getDrugPeriod() {
		return drugPeriod;
	}
	
	public void setStartTime(Date start_time) {
		this.setStartTime = start_time;
	}
	
	public Date getStartTime() {
		return setStartTime;
	}

}
