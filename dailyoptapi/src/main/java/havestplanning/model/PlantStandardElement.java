package havestplanning.model;

public class PlantStandardElement {
	private String category;
	private String plantType;
		
	private int plantPeriod;
	private double result;
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
	public int getPlantPeriod() {
		return plantPeriod;
	}
	public void setPlantPeriod(int plantPeriod) {
		this.plantPeriod = plantPeriod;
	}
	public double getResult() {
		return result;
	}
	public void setResult(double result) {
		this.result = result;
	}
	public PlantStandardElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PlantStandardElement(String category, String plantType,
			int plantPeriod, double result) {
		super();
		this.category = category;
		this.plantType = plantType;
		this.plantPeriod = plantPeriod;
		this.result = result;
	}
	
	
}
