package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Truck {
	private String code;
	private double weight;
	private String depotTruckCode;
	private String startWorkingTime;
	private String endWorkingTime;
	private String[] returnDepotCodes;// possible depots when finishing services
	
	
	public Truck(String code, double weight, String depotTruckCode, String startWorkingTime,
			String endWorkingTime, String[] returnDepotCodes) {
		super();
		this.code = code;
		this.weight = weight;
		this.depotTruckCode = depotTruckCode;
		this.startWorkingTime = startWorkingTime;
		this.endWorkingTime = endWorkingTime;
		this.returnDepotCodes = returnDepotCodes;
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
	
	public Truck() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
