package com.havestplanning.model;

import java.util.Date;

public class HavestPlanningCluster {
	private String date;
	private int quantity;
	private int numberOfFields;
	private HavestPlanningField[] fields;
	
	private double sugarQuantity;
	
	
	public double getSugarQuantity() {
		return sugarQuantity;
	}
	public void setSugarQuantity(double sugarQuantity) {
		this.sugarQuantity = sugarQuantity;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDate() {
		return date;
	}
	public void setDates(String date) {
		this.date = date;
	}
	public HavestPlanningField[] getFields() {
		return fields;
	}
	public void setFields(HavestPlanningField[] fields) {
		this.fields = fields;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getNumberOfFields() {
		return numberOfFields;
	}
	public void setNumberOfFields(int numberOfFields) {
		this.numberOfFields = numberOfFields;
	}
	public HavestPlanningCluster() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public HavestPlanningCluster(String date, int quantity, int numberOfFields,
			HavestPlanningField[] fields, double sugarQuantity) {
		super();
		this.date = date;
		this.quantity = quantity;
		this.numberOfFields = numberOfFields;
		this.fields = fields;
		this.sugarQuantity = sugarQuantity;
	}
	
	// additional methods
	public void concate(HavestPlanningCluster C){
		this.quantity = this.quantity + C.getQuantity();
		this.numberOfFields = this.numberOfFields + C.getNumberOfFields();
		HavestPlanningField[] newFields = new HavestPlanningField[fields.length + C.getFields().length];
		for(int i = 0; i < fields.length; i++)
			newFields[i] = fields[i];
		for(int i = 0; i < C.getFields().length; i++){
			newFields[i + fields.length] = C.getFields()[i];
		}
		fields = newFields;
	}
	
	
}
