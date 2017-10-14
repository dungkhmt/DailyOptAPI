package havestplanning.model;

import havestplanning.model.vn.HavestPlanningFieldVN;

import java.util.Date;

public class HavestPlanningField {
	private Field field;
	private String expected_havest_date;
	private int havestQuantity;
	private int days_late;
	private	double sugarQuantity;
	
	
	public double getSugarQuantity() {
		return sugarQuantity;
	}
	public void setSugarQuantity(double sugarQuantity) {
		this.sugarQuantity = sugarQuantity;
	}
	public int getHavestQuantity() {
		return havestQuantity;
	}
	public void setHavestQuantity(int havestQuantity) {
		this.havestQuantity = havestQuantity;
	}
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public String getExpected_havest_date() {
		return expected_havest_date;
	}
	public void setExpected_havest_date(String expected_havest_date) {
		this.expected_havest_date = expected_havest_date;
	}
	public int getDays_late() {
		return days_late;
	}
	public void setDays_late(int days_late) {
		this.days_late = days_late;
	}

	public HavestPlanningField() {
		super();
		// TODO Auto-generated constructor stub
	}
	public HavestPlanningField(Field field, String expected_havest_date,
			int havestQuantity, int days_late, double sugarQuantity) {
		super();
		this.field = field;
		this.expected_havest_date = expected_havest_date;
		this.havestQuantity = havestQuantity;
		this.days_late = days_late;
		this.sugarQuantity = sugarQuantity;
	}
	
	
}
