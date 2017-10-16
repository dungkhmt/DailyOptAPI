package com.havestplanning.model;

public class HavestPlanningInput {
	private Field[] fields;
	
	/*
	private QualityFunction qualityFunction;
	private int minP;
	private int maxP;
	private int clusterDuration; 
	private int growthDuration;
	*/
	
	private PlantStandard plantStandard;
	private MachineSetting machineSetting;
	
	public String checkConsistency(){
		String des = "";
		boolean ok = true;
		for(int i = 0; i < fields.length; i++){
			if(!plantStandard.find(fields[i].getCategory(), fields[i].getPlantType())){
				des += "fields " + fields[i].getCode() + " NOT in plat-standard\n";
				ok = false;
			}
		}
		if(ok) des = "OK";
		return des;
	}
	public PlantStandard getPlantStandard() {
		return plantStandard;
	}
	public void setPlantStandard(PlantStandard plantStandard) {
		this.plantStandard = plantStandard;
	}
	/*
	public int getGrowthDuration() {
		return growthDuration;
	}
	public void setGrowthDuration(int growthDuration) {
		this.growthDuration = growthDuration;
	}
	public int getClusterDuration() {
		return clusterDuration;
	}
	public void setClusterDuration(int clusterDuration) {
		this.clusterDuration = clusterDuration;
	}
	
	public int getMinP() {
		return minP;
	}
	public void setMinP(int minP) {
		this.minP = minP;
	}
	public int getMaxP() {
		return maxP;
	}
	public void setMaxP(int maxP) {
		this.maxP = maxP;
	}
	public QualityFunction getQualityFunction() {
		return qualityFunction;
	}
	public void setQualityFunction(QualityFunction qualityFunction) {
		this.qualityFunction = qualityFunction;
	}
	*/
	public Field[] getFields() {
		return fields;
	}
	public void setFields(Field[] fields) {
		this.fields = fields;
	}
	/*
	public HavestPlanningInput(Field[] fields, QualityFunction qualityFunction,
			int minP, int maxP, int clusterDuration, int growthDuration) {
		super();
		this.fields = fields;
		this.qualityFunction = qualityFunction;
		this.minP = minP;
		this.maxP = maxP;
		this.clusterDuration = clusterDuration;
		this.growthDuration = growthDuration;
		
	}
	
	public HavestPlanningInput(Field[] fields, QualityFunction qualityFunction,
			int minP, int maxP, int clusterDuration, int growthDuration,
			PlantStandard plantStandard) {
		super();
		this.fields = fields;
		this.qualityFunction = qualityFunction;
		this.minP = minP;
		this.maxP = maxP;
		this.clusterDuration = clusterDuration;
		this.growthDuration = growthDuration;
		this.plantStandard = plantStandard;
	}
	*/
	
	
	public HavestPlanningInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public MachineSetting getMachineSetting() {
		return machineSetting;
	}
	public void setMachineSetting(MachineSetting machineSetting) {
		this.machineSetting = machineSetting;
	}
	public HavestPlanningInput(Field[] fields, PlantStandard plantStandard,
			MachineSetting machineSetting) {
		super();
		this.fields = fields;
		this.plantStandard = plantStandard;
		this.machineSetting = machineSetting;
	}
	// additional methods
	public void initDefaultPlantStandard(){
		PlantStandardElement[] pse = new PlantStandardElement[3];
		pse[0] = new PlantStandardElement("-", "-", 355, 1);
		pse[1] = new PlantStandardElement("-", "-", 350, 0.5);
		pse[2] = new PlantStandardElement("-", "-", 360, 0.5);
		this.plantStandard = new PlantStandard(pse);
	
	}
	
}
