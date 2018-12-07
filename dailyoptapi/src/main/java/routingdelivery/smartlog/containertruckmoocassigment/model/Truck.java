package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Truck {
	private String code;
	private double weight;
	private String driverCode;
	private String driverName;
	private String depotTruckCode;
	private String startWorkingTime;
	private String endWorkingTime;
	private String status;
	private String[] returnDepotCodes;// possible depots when finishing services
	private Intervals[] intervals;
	
	public Truck(String code, double weight, String driverCode, String driverName, String depotTruckCode, String startWorkingTime,
			String endWorkingTime, String status, String[] returnDepotCodes, Intervals[] intervals) {
		super();
		this.code = code;
		this.weight = weight;
		this.driverCode = driverCode;
		this.driverName = driverName;
		this.depotTruckCode = depotTruckCode;
		this.startWorkingTime = startWorkingTime;
		this.endWorkingTime = endWorkingTime;
		this.status = status;
		this.returnDepotCodes = returnDepotCodes;
		this.intervals = intervals;
	}
	public String[] getReturnDepotCodes() {
		return returnDepotCodes;
	}
	public void setReturnDepotCodes(String[] returnDepotCodes) {
		this.returnDepotCodes = returnDepotCodes;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public String getDriverCode() {
		return driverCode;
	}
	public void setDriverCode(String driverCode) {
		this.driverCode = driverCode;
	}
	
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getDepotTruckCode() {
		return depotTruckCode;
	}
	public void setDepotTruckCode(String depotTruckCode) {
		this.depotTruckCode = depotTruckCode;
	}
	public String getStartWorkingTime() {
		return startWorkingTime;
	}
	public void setStartWorkingTime(String startWorkingTime) {
		this.startWorkingTime = startWorkingTime;
	}
	public String getEndWorkingTime() {
		return endWorkingTime;
	}
	public void setEndWorkingTime(String endWorkingTime) {
		this.endWorkingTime = endWorkingTime;
	}
	public String getStatus(){
		return this.status;
	}
	public void setStatus(String status){
		this.status = status;
	}
	public Intervals[] getIntervals() {
		return intervals;
	}
	public void setIntervals(Intervals[] intervals) {
		this.intervals = intervals;
	}
	public Truck() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
