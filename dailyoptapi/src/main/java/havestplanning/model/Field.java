package havestplanning.model;

import java.util.Date;

public class Field {
	private String code;
	private String districtCode;
	private String ownerCode;
	private double area;
	
	//private Date date;// optimal havesting date
	private String plant_date;
	
	private int quantity;
	
	private String category;
	private String plantType;
	
	private int deltaDays;
	
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getPlantType() {
		return plantType;
	}
	public void setPlantType(String plantType) {
		this.plantType = plantType;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDistrictCode() {
		return districtCode;
	}
	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}
	public String getOwnerCode() {
		return ownerCode;
	}
	public void setOwnerCode(String ownerCode) {
		this.ownerCode = ownerCode;
	}
	public double getArea() {
		return area;
	}
	public void setArea(double area) {
		this.area = area;
	}
	public String getPlant_date() {
		return plant_date;
	}
	public void setPlant_date(String plant_date) {
		this.plant_date = plant_date;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getDeltaDays() {
		return deltaDays;
	}
	public void setDeltaDays(int deltaDays) {
		this.deltaDays = deltaDays;
	}
	public Field(String code, String districtCode, String ownerCode,
			double area, String plant_date, int quantity, int deltaDays) {
		super();
		this.code = code;
		this.districtCode = districtCode;
		this.ownerCode = ownerCode;
		this.area = area;
		this.plant_date = plant_date;
		this.quantity = quantity;
		this.deltaDays = deltaDays;
		
		this.category = "-";
		this.plantType = "-";
	}
	
	public Field(String code, String districtCode, String ownerCode,
			double area, String plant_date, int quantity, String category,
			String plantType, int deltaDays) {
		super();
		this.code = code;
		this.districtCode = districtCode;
		this.ownerCode = ownerCode;
		this.area = area;
		this.plant_date = plant_date;
		this.quantity = quantity;
		this.category = category;
		this.plantType = plantType;
		this.deltaDays = deltaDays;
	}
	
	public Field() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
