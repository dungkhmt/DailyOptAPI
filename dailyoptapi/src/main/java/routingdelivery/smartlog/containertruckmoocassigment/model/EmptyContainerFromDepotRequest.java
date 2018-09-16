package routingdelivery.smartlog.containertruckmoocassigment.model;

public class EmptyContainerFromDepotRequest {
	private String orderID;
	
	private String shipCompanyCode;
	private String[] depotContainerCode; //select a container from one of these depots
	private String containerCategory;// 20, 40, 45
	
	private String toLocationCode;// destination location
	private String earlyArrivalDateTime;
	private String lateArrivalDateTime;// early and late arrival time to toLocationCode
	private int detachLoadedMoocContainerDuration;
	
	
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
			String containerCategory, String toLocationCode,
			String earlyArrivalDateTime, String lateArrivalDateTime,
			int detachLoadedMoocContainerDuration) {
		super();
		this.orderID = orderID;
		this.shipCompanyCode = shipCompanyCode;
		this.depotContainerCode = depotContainerCode;
		this.containerCategory = containerCategory;
		this.toLocationCode = toLocationCode;
		this.earlyArrivalDateTime = earlyArrivalDateTime;
		this.lateArrivalDateTime = lateArrivalDateTime;
		this.detachLoadedMoocContainerDuration = detachLoadedMoocContainerDuration;
	}
	public EmptyContainerFromDepotRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
