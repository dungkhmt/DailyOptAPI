package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Truck {
	private String code;
	private String depotTruckCode;
	private String startWorkingTime;
	private String endWorkingTime;
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
	public Truck(String code, String depotTruckCode, String startWorkingTime,
			String endWorkingTime) {
		super();
		this.code = code;
		this.depotTruckCode = depotTruckCode;
		this.startWorkingTime = startWorkingTime;
		this.endWorkingTime = endWorkingTime;
	}
	public Truck() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}