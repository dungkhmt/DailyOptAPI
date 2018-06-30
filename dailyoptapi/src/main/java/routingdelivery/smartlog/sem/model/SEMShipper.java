package routingdelivery.smartlog.sem.model;

public class SEMShipper {
	private String shipperID;
	private double weightCapacity;
	private int maxOrder;
	private int maxAmountMoney;
	private String startWorkingTime;
	private String endWorkingTime;
	private double startLat;
	private double startLng;
	private String startLocationCode;
	private double endLat;
	private double endLng;
	private String endLocationCode;
	
	
	public String getStartLocationCode() {
		return startLocationCode;
	}
	public void setStartLocationCode(String startLocationCode) {
		this.startLocationCode = startLocationCode;
	}
	public String getEndLocationCode() {
		return endLocationCode;
	}
	public void setEndLocationCode(String endLocationCode) {
		this.endLocationCode = endLocationCode;
	}
	public String getShipperID() {
		return shipperID;
	}
	public void setShipperID(String shipperID) {
		this.shipperID = shipperID;
	}
	public double getWeightCapacity() {
		return weightCapacity;
	}
	public void setWeightCapacity(double weightCapacity) {
		this.weightCapacity = weightCapacity;
	}
	public int getMaxOrder() {
		return maxOrder;
	}
	public void setMaxOrder(int maxOrder) {
		this.maxOrder = maxOrder;
	}
	public int getMaxAmountMoney() {
		return maxAmountMoney;
	}
	public void setMaxAmountMoney(int maxAmountMoney) {
		this.maxAmountMoney = maxAmountMoney;
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
	public double getStartLat() {
		return startLat;
	}
	public void setStartLat(double startLat) {
		this.startLat = startLat;
	}
	public double getStartLng() {
		return startLng;
	}
	public void setStartLng(double startLng) {
		this.startLng = startLng;
	}
	public double getEndLat() {
		return endLat;
	}
	public void setEndLat(double endLat) {
		this.endLat = endLat;
	}
	public double getEndLng() {
		return endLng;
	}
	public void setEndLng(double endLng) {
		this.endLng = endLng;
	}
	public SEMShipper() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SEMShipper(String shipperID, double weightCapacity, int maxOrder,
			int maxAmountMoney, String startWorkingTime, String endWorkingTime,
			double startLat, double startLng, String startLocationCode,
			double endLat, double endLng, String endLocationCode) {
		super();
		this.shipperID = shipperID;
		this.weightCapacity = weightCapacity;
		this.maxOrder = maxOrder;
		this.maxAmountMoney = maxAmountMoney;
		this.startWorkingTime = startWorkingTime;
		this.endWorkingTime = endWorkingTime;
		this.startLat = startLat;
		this.startLng = startLng;
		this.startLocationCode = startLocationCode;
		this.endLat = endLat;
		this.endLng = endLng;
		this.endLocationCode = endLocationCode;
	}
	
	
	
}
