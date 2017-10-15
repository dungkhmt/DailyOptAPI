package havestplanning.model.vn;

import havestplanning.model.HavestPlanningCluster;
import havestplanning.model.HavestPlanningField;
import havestplanning.model.HavestPlanningSolution;

public class HavestPlanningSolutionVN {
	private double tong_luong_duong;
	private String mo_ta;
	
	
	// statistics information 
	private int numberOfFieldsInPlan;
	private int numberOfDatesInPlan;
	private int numberOfDatesInPlantStandard;
	private int initMinQuantityDay;
	private int initMaxQuantityDay;
	private int computedMinQuantityDay;
	private int computedMaxQuantityDay;
	private int numberFieldsNotPlanned;
	private int quantityNotPlanned;
	private int quantityPlanned;
	private int totalQuantity;
	private int numberOfLevels;
	private int numberOfDaysHarvestExact;
	private int numberOfDaysPlanned;
	private int numberOfFieldsCompleted;
	private int maxDaysLate;
	private int maxDaysEarly;
	private int numberOfDaysOverLoad;
	private int numberOfDaysUnderLoad;
	
	private HavestPlanningClusterVN[] clusters;

	public void convertFrom(HavestPlanningSolution s){
		tong_luong_duong = s.getQuality();
		mo_ta = s.getDescription();
		numberOfFieldsInPlan = s.getNumberOfFieldsInPlan();
		numberOfDatesInPlan = s.getNumberOfDatesInPlan();
		numberOfDatesInPlantStandard = s.getNumberOfDatesInPlantStandard();
		initMinQuantityDay = s.getInitMinQuantityDay();
		initMaxQuantityDay = s.getInitMaxQuantityDay();
		computedMinQuantityDay = s.getComputedMinQuantityDay();
		computedMaxQuantityDay = s.getComputedMaxQuantityDay();
		numberFieldsNotPlanned = s.getNumberFieldsNotPlanned();
		quantityNotPlanned = s.getQuantityNotPlanned();
		quantityPlanned = s.getQuantityPlanned();
		totalQuantity = s.getTotalQuantity();
		numberOfLevels = s.getNumberOfLevels();
		numberOfDaysHarvestExact = s.getNumberOfDaysHarvestExact();
		numberOfDaysPlanned = s.getNumberOfDaysPlanned();
		numberOfFieldsCompleted = s.getNumberOfFieldsCompleted();
		maxDaysLate = s.getMaxDaysLate();
		maxDaysEarly = s.getMaxDaysEarly();
		numberOfDaysOverLoad = s.getNumberOfDaysOverLoad();
		numberOfDaysUnderLoad = s.getNumberOfDaysUnderLoad();
		
		HavestPlanningClusterVN[] clusters = new HavestPlanningClusterVN[s.getClusters().length];
		for(int i = 0; i < clusters.length; i++){
			HavestPlanningFieldVN[] fields = new HavestPlanningFieldVN[s.getClusters()[i].getFields().length];
			for(int j = 0; j < fields.length; j++){
				HavestPlanningField F = s.getClusters()[i].getFields()[j];
				fields[i] = new HavestPlanningFieldVN(F.getField().getCode(), 
						F.getField().getPlant_date(), 
						(float)F.getField().getArea(), 
						0, 
						(float)F.getField().getQuantity(), 
						s.getClusters()[i].getDate(), 
						(float)F.getSugarQuantity());
			}
			clusters[i] = new HavestPlanningClusterVN(s.getClusters()[i].getDate(), 
					s.getClusters()[i].getQuantity(), s.getClusters()[i].getNumberOfFields(), 
					fields, s.getClusters()[i].getSugarQuantity());
		}
	}
	public double getTong_luong_duong() {
		return tong_luong_duong;
	}

	public void setTong_luong_duong(double tong_luong_duong) {
		tong_luong_duong = tong_luong_duong;
	}

	public String getMo_ta() {
		return mo_ta;
	}

	public void setMo_ta(String mo_ta) {
		mo_ta = mo_ta;
	}

	public int getNumberOfFieldsInPlan() {
		return numberOfFieldsInPlan;
	}

	public void setNumberOfFieldsInPlan(int numberOfFieldsInPlan) {
		this.numberOfFieldsInPlan = numberOfFieldsInPlan;
	}

	public int getNumberOfDatesInPlan() {
		return numberOfDatesInPlan;
	}

	public void setNumberOfDatesInPlan(int numberOfDatesInPlan) {
		this.numberOfDatesInPlan = numberOfDatesInPlan;
	}

	public int getNumberOfDatesInPlantStandard() {
		return numberOfDatesInPlantStandard;
	}

	public void setNumberOfDatesInPlantStandard(int numberOfDatesInPlantStandard) {
		this.numberOfDatesInPlantStandard = numberOfDatesInPlantStandard;
	}

	public int getInitMinQuantityDay() {
		return initMinQuantityDay;
	}

	public void setInitMinQuantityDay(int initMinQuantityDay) {
		this.initMinQuantityDay = initMinQuantityDay;
	}

	public int getInitMaxQuantityDay() {
		return initMaxQuantityDay;
	}

	public void setInitMaxQuantityDay(int initMaxQuantityDay) {
		this.initMaxQuantityDay = initMaxQuantityDay;
	}

	public int getComputedMinQuantityDay() {
		return computedMinQuantityDay;
	}

	public void setComputedMinQuantityDay(int computedMinQuantityDay) {
		this.computedMinQuantityDay = computedMinQuantityDay;
	}

	public int getComputedMaxQuantityDay() {
		return computedMaxQuantityDay;
	}

	public void setComputedMaxQuantityDay(int computedMaxQuantityDay) {
		this.computedMaxQuantityDay = computedMaxQuantityDay;
	}

	public int getNumberFieldsNotPlanned() {
		return numberFieldsNotPlanned;
	}

	public void setNumberFieldsNotPlanned(int numberFieldsNotPlanned) {
		this.numberFieldsNotPlanned = numberFieldsNotPlanned;
	}

	public int getQuantityNotPlanned() {
		return quantityNotPlanned;
	}

	public void setQuantityNotPlanned(int quantityNotPlanned) {
		this.quantityNotPlanned = quantityNotPlanned;
	}

	public int getQuantityPlanned() {
		return quantityPlanned;
	}

	public void setQuantityPlanned(int quantityPlanned) {
		this.quantityPlanned = quantityPlanned;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public int getNumberOfLevels() {
		return numberOfLevels;
	}

	public void setNumberOfLevels(int numberOfLevels) {
		this.numberOfLevels = numberOfLevels;
	}

	public int getNumberOfDaysHarvestExact() {
		return numberOfDaysHarvestExact;
	}

	public void setNumberOfDaysHarvestExact(int numberOfDaysHarvestExact) {
		this.numberOfDaysHarvestExact = numberOfDaysHarvestExact;
	}

	public int getNumberOfDaysPlanned() {
		return numberOfDaysPlanned;
	}

	public void setNumberOfDaysPlanned(int numberOfDaysPlanned) {
		this.numberOfDaysPlanned = numberOfDaysPlanned;
	}

	public int getNumberOfFieldsCompleted() {
		return numberOfFieldsCompleted;
	}

	public void setNumberOfFieldsCompleted(int numberOfFieldsCompleted) {
		this.numberOfFieldsCompleted = numberOfFieldsCompleted;
	}

	public int getMaxDaysLate() {
		return maxDaysLate;
	}

	public void setMaxDaysLate(int maxDaysLate) {
		this.maxDaysLate = maxDaysLate;
	}

	public int getMaxDaysEarly() {
		return maxDaysEarly;
	}

	public void setMaxDaysEarly(int maxDaysEarly) {
		this.maxDaysEarly = maxDaysEarly;
	}

	public int getNumberOfDaysOverLoad() {
		return numberOfDaysOverLoad;
	}

	public void setNumberOfDaysOverLoad(int numberOfDaysOverLoad) {
		this.numberOfDaysOverLoad = numberOfDaysOverLoad;
	}

	public int getNumberOfDaysUnderLoad() {
		return numberOfDaysUnderLoad;
	}

	public void setNumberOfDaysUnderLoad(int numberOfDaysUnderLoad) {
		this.numberOfDaysUnderLoad = numberOfDaysUnderLoad;
	}

	public HavestPlanningClusterVN[] getClusters() {
		return clusters;
	}

	public void setClusters(HavestPlanningClusterVN[] clusters) {
		this.clusters = clusters;
	}

	public HavestPlanningSolutionVN(double tong_luong_duong, String mo_ta,
			int numberOfFieldsInPlan, int numberOfDatesInPlan,
			int numberOfDatesInPlantStandard, int initMinQuantityDay,
			int initMaxQuantityDay, int computedMinQuantityDay,
			int computedMaxQuantityDay, int numberFieldsNotPlanned,
			int quantityNotPlanned, int quantityPlanned, int totalQuantity,
			int numberOfLevels, int numberOfDaysHarvestExact,
			int numberOfDaysPlanned, int numberOfFieldsCompleted,
			int maxDaysLate, int maxDaysEarly, int numberOfDaysOverLoad,
			int numberOfDaysUnderLoad, HavestPlanningClusterVN[] clusters) {
		super();
		tong_luong_duong = tong_luong_duong;
		mo_ta = mo_ta;
		this.numberOfFieldsInPlan = numberOfFieldsInPlan;
		this.numberOfDatesInPlan = numberOfDatesInPlan;
		this.numberOfDatesInPlantStandard = numberOfDatesInPlantStandard;
		this.initMinQuantityDay = initMinQuantityDay;
		this.initMaxQuantityDay = initMaxQuantityDay;
		this.computedMinQuantityDay = computedMinQuantityDay;
		this.computedMaxQuantityDay = computedMaxQuantityDay;
		this.numberFieldsNotPlanned = numberFieldsNotPlanned;
		this.quantityNotPlanned = quantityNotPlanned;
		this.quantityPlanned = quantityPlanned;
		this.totalQuantity = totalQuantity;
		this.numberOfLevels = numberOfLevels;
		this.numberOfDaysHarvestExact = numberOfDaysHarvestExact;
		this.numberOfDaysPlanned = numberOfDaysPlanned;
		this.numberOfFieldsCompleted = numberOfFieldsCompleted;
		this.maxDaysLate = maxDaysLate;
		this.maxDaysEarly = maxDaysEarly;
		this.numberOfDaysOverLoad = numberOfDaysOverLoad;
		this.numberOfDaysUnderLoad = numberOfDaysUnderLoad;
		this.clusters = clusters;
	}

	public HavestPlanningSolutionVN() {
		super();
		// TODO Auto-generated constructor stub
	}

}
