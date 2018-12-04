package routingdelivery.smartlog.containertruckmoocassigment.model;

public class EmptyContainerFromDepotRequest {
	private String orderID;
	
	private String shipCompanyCode;
	private String[] depotContainerCode; //select a container from one of these depots
	private String containerCategory;// 20, 40, 45
	private String containerCode;
	private String containerNo;
	private double weight;
	
	private String toLocationCode;// destination location
	private String earlyArrivalDateTime;
	private String lateArrivalDateTime;// early and late arrival time to toLocationCode
	private int detachLoadedMoocContainerDuration;
	private String customerCode;
	private String customerName;
	
	
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public String getShipCompanyCode() {
		return shipCompanyCode;
	}
	public void setShipCompanyCode(String shipCompanyCode) {
		this.shipCompanyCode = shipCompanyCode;
	}
	public String[] getDepotContainerCode() {
		return depotContainerCode;
	}
	public void setDepotContainerCode(String[] depotContainerCode) {
		this.depotContainerCode = depotContainerCode;
	}
	public String getContainerCategory() {
		return containerCategory;
	}
	public void setContainerCategory(String containerCategory) {
		this.containerCategory = containerCategory;
	}
	public String getToLocationCode() {
		return toLocationCode;
	}
	public void setToLocationCode(String toLocationCode) {
		this.toLocationCode = toLocationCode;
	}
	public String getEarlyArrivalDateTime() {
		return earlyArrivalDateTime;
	}
	public void setEarlyArrivalDateTime(String earlyArrivalDateTime) {
		this.earlyArrivalDateTime = earlyArrivalDateTime;
	}
	public String getLateArrivalDateTime() {
		return lateArrivalDateTime;
	}
	public void setLateArrivalDateTime(String lateArrivalDateTime) {
		this.lateArrivalDateTime = lateArrivalDateTime;
	}
	public int getDetachLoadedMoocContainerDuration() {
		return detachLoadedMoocContainerDuration;
	}
	public void setDetachLoadedMoocContainerDuration(
			int detachLoadedMoocContainerDuration) {
		this.detachLoadedMoocContainerDuration = detachLoadedMoocContainerDuration;
	}
	public EmptyContainerFromDepotRequest(String orderID,
			String shipCompanyCode, String[] depotContainerCode,
			String containerCategory, String containerCode, String containerNo, 
			double weight, String toLocationCode,
			String earlyArrivalDateTime, String lateArrivalDateTime,
			int detachLoadedMoocContainerDuration, String customerCode, String customerNo) {
		super();
		this.orderID = orderID;
		this.shipCompanyCode = shipCompanyCode;
		this.depotContainerCode = depotContainerCode;
		this.containerCategory = containerCategory;
		this.containerCode = containerCode;
		this.containerNo = containerNo;
		this.weight = weight;
		this.toLocationCode = toLocationCode;
		this.earlyArrivalDateTime = earlyArrivalDateTime;
		this.lateArrivalDateTime = lateArrivalDateTime;
		this.detachLoadedMoocContainerDuration = detachLoadedMoocContainerDuration;
		this.customerCode = customerCode;
		this.containerNo = customerNo;
	}
	public EmptyContainerFromDepotRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getContainerCode() {
		return containerCode;
	}
	public void setContainerCode(String containerCode) {
		this.containerCode = containerCode;
	}
	public String getContainerNo() {
		return containerNo;
	}
	public void setContainerNo(String containerNo) {
		this.containerNo = containerNo;
	}
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public String getCustomerCode(){
		return this.customerCode;
	}
	
	public void setCustomerCode(String customerCode){
		this.customerCode = customerCode;
	}
	
	public String getCustomerName(){
		return this.customerName;
	}
	
	public void setCustomerName(String customerName){
		this.customerName = customerName;
	}
	
}
