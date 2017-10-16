package com.havestplanning.model;

public class ReturnPlantStandard {
	private String description;
	private PlantStandard plantStandard;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public PlantStandard getPlantStandard() {
		return plantStandard;
	}
	public void setPlantStandard(PlantStandard plantStandard) {
		this.plantStandard = plantStandard;
	}
	public ReturnPlantStandard(String description, PlantStandard plantStandard) {
		super();
		this.description = description;
		this.plantStandard = plantStandard;
	}
	public ReturnPlantStandard() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
