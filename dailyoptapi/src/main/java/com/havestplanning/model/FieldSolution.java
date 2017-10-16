package com.havestplanning.model;

public class FieldSolution extends Field{
	private String havest_date;
	private int havest_quantity;
	public String getHavest_date() {
		return havest_date;
	}
	public void setHavest_date(String havest_date) {
		this.havest_date = havest_date;
	}
	public int getHavest_quantity() {
		return havest_quantity;
	}
	public void setHavest_quantity(int havest_quantity) {
		this.havest_quantity = havest_quantity;
	}
	public FieldSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FieldSolution(String code, String districtCode, String ownerCode,
			double area, String plant_date, int quantity, int deltaDays) {
		super(code, districtCode, ownerCode, area, plant_date, quantity, deltaDays);
		// TODO Auto-generated constructor stub
	}
	public FieldSolution(String code, String districtCode, String ownerCode,
			double area, String plant_date, int quantity, String category,
			String plantType, int deltaDays) {
		super(code, districtCode, ownerCode, area, plant_date, quantity, category,
				plantType, deltaDays);
		// TODO Auto-generated constructor stub
	}
	public FieldSolution(String code, String districtCode, String ownerCode,
			double area, String plant_date, int quantity, int deltaDays,
			String havest_date, int havest_quantity) {
		super(code, districtCode, ownerCode, area, plant_date, quantity,
				deltaDays);
		this.havest_date = havest_date;
		this.havest_quantity = havest_quantity;
	}
	
}
