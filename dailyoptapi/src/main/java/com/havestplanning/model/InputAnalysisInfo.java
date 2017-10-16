package com.havestplanning.model;

public class InputAnalysisInfo {
	private String earliestPlantDate;
	private String latestPlantDate;
	private int numberOfFields;
	private int totalQuantity;
	private int numberOfDatesPlantRange;
	private int maxQuantity;
	private int minQuantity;
	public String getEarliestPlantDate() {
		return earliestPlantDate;
	}
	public void setEarliestPlantDate(String earliestPlantDate) {
		this.earliestPlantDate = earliestPlantDate;
	}
	public String getLatestPlantDate() {
		return latestPlantDate;
	}
	public void setLatestPlantDate(String latestPlantDate) {
		this.latestPlantDate = latestPlantDate;
	}
	public int getNumberOfFields() {
		return numberOfFields;
	}
	public void setNumberOfFields(int numberOfFields) {
		this.numberOfFields = numberOfFields;
	}
	public int getTotalQuantity() {
		return totalQuantity;
	}
	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	public int getNumberOfDatesPlantRange() {
		return numberOfDatesPlantRange;
	}
	public void setNumberOfDatesPlantRange(int numberOfDatesPlantRange) {
		this.numberOfDatesPlantRange = numberOfDatesPlantRange;
	}
	public int getMaxQuantity() {
		return maxQuantity;
	}
	public void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}
	public int getMinQuantity() {
		return minQuantity;
	}
	public void setMinQuantity(int minQuantity) {
		this.minQuantity = minQuantity;
	}
	public InputAnalysisInfo(String earliestPlantDate, String latestPlantDate,
			int numberOfFields, int totalQuantity, int numberOfDatesPlantRange,
			int maxQuantity, int minQuantity) {
		super();
		this.earliestPlantDate = earliestPlantDate;
		this.latestPlantDate = latestPlantDate;
		this.numberOfFields = numberOfFields;
		this.totalQuantity = totalQuantity;
		this.numberOfDatesPlantRange = numberOfDatesPlantRange;
		this.maxQuantity = maxQuantity;
		this.minQuantity = minQuantity;
	}
	public InputAnalysisInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
