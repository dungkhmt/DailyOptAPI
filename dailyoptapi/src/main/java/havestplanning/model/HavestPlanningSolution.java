package havestplanning.model;

public class HavestPlanningSolution {
	private double quality;
	private String description;
	
	
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
	
	private HavestPlanningCluster[] clusters;
	
	
	public void sort(){
		for(int i= 0; i < clusters.length; i++){
			for(int j = i+1; j < clusters.length; j++){
				if(clusters[i].getQuantity() < clusters[j].getQuantity()){
					HavestPlanningCluster tmp = clusters[i];
					clusters[i] = clusters[j];
					clusters[j] = tmp;
				}
			}
		}
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public HavestPlanningCluster[] getClusters() {
		return clusters;
	}
	public void setClusters(HavestPlanningCluster[] clusters) {
		this.clusters = clusters;
	}
	public double getQuality() {
		return quality;
	}
	public void setQuality(double quality) {
		this.quality = quality;
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
	public HavestPlanningSolution(HavestPlanningCluster[] clusters,
			double quality) {
		super();
		this.clusters = clusters;
		this.quality = quality;
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
	public HavestPlanningSolution(double quality, String description,
			HavestPlanningCluster[] clusters) {
		super();
		this.quality = quality;
		this.description = description;
		this.clusters = clusters;
	}

	public HavestPlanningSolution(double quality, String description,
			int numberOfFieldsInPlan, int numberOfDatesInPlan,
			int numberOfDatesInPlantStandard, int initMinQuantityDay,
			int initMaxQuantityDay, int computedMinQuantityDay,
			int computedMaxQuantityDay, int numberFieldsNotPlanned,
			int quantityNotPlanned, int quantityPlanned, int totalQuantity, int numberOfLevels) {
		super();
		this.quality = quality;
		this.description = description;
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
	
	public HavestPlanningSolution(double quality, String description,
			int numberOfFieldsInPlan, int numberOfDatesInPlan,
			int numberOfDatesInPlantStandard, int initMinQuantityDay,
			int initMaxQuantityDay, int computedMinQuantityDay,
			int computedMaxQuantityDay, int numberFieldsNotPlanned,
			int quantityNotPlanned, int quantityPlanned, int totalQuantity,
			int numberOfLevels, int numberOfDaysHarvestExact,
			int numberOfDaysPlanned, int numberOfFieldsCompleted,
			int maxDaysLate, int maxDaysEarly, int numberOfDaysOverLoad,
			int numberOfDaysUnderLoad) {
		super();
		this.quality = quality;
		this.description = description;
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
	}
	public HavestPlanningSolution(double quality, String description,
			int numberOfFieldsInPlan, int numberOfDatesInPlan,
			int numberOfDatesInPlantStandard, int initMinQuantityDay,
			int initMaxQuantityDay, int computedMinQuantityDay,
			int computedMaxQuantityDay, int numberFieldsNotPlanned,
			int quantityNotPlanned, int quantityPlanned, int totalQuantity,
			int numberOfLevels, int numberOfDaysHarvestExact,
			int numberOfDaysPlanned, int numberOfFieldsCompleted,
			int maxDaysLate, int maxDaysEarly) {
		super();
		this.quality = quality;
		this.description = description;
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
	}
	public HavestPlanningSolution(double quality, String description,
			int numberOfFieldsInPlan, int numberOfDatesInPlan,
			int numberOfDatesInPlantStandard, int initMinQuantityDay,
			int initMaxQuantityDay, int computedMinQuantityDay,
			int computedMaxQuantityDay, int numberFieldsNotPlanned,
			int quantityNotPlanned, int quantityPlanned, int totalQuantity, HavestPlanningCluster[] clusters) {
		super();
		this.quality = quality;
		this.description = description;
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
		
		this.clusters = clusters;
	}
	public HavestPlanningSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
